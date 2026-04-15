package com.modeai.core.service;

import com.modeai.api.dto.DocumentQueryRequest;
import com.modeai.api.dto.DocumentUploadResponse;
import com.modeai.common.dto.PageResult;
import com.modeai.common.exception.BusinessException;
import com.modeai.core.domain.entity.Document;
import com.modeai.core.domain.entity.DocumentChunk;
import com.modeai.core.domain.entity.User;
import com.modeai.core.domain.repository.DocumentChunkRepository;
import com.modeai.core.domain.repository.DocumentRepository;
import com.modeai.core.domain.repository.UserRepository;
import com.modeai.infra.mq.DocumentProcessPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final UserRepository userRepository;
    private final DocumentProcessPublisher documentProcessPublisher;

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Override
    @Transactional
    public DocumentUploadResponse uploadDocument(MultipartFile file, Long departmentId,
                                                  Integer accessLevel, String username) {
        if (file.isEmpty()) {
            throw new BusinessException(400, "文件不能为空");
        }

        // Validate file type
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        if (!isSupportedFileType(fileExtension)) {
            throw new BusinessException(400, "不支持的文件类型: " + fileExtension + "。支持: pdf, docx, doc, md, txt");
        }

        try {
            // Create upload directory
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Save file
            String storedFileName = UUID.randomUUID() + "." + fileExtension;
            Path filePath = uploadPath.resolve(storedFileName);
            file.transferTo(filePath.toFile());

            // Get user
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new BusinessException(401, "用户不存在"));

            // Create document record
            Document document = Document.builder()
                    .title(originalFilename)
                    .fileName(originalFilename)
                    .filePath(filePath.toString())
                    .fileSize(file.getSize())
                    .fileType(fileExtension)
                    .departmentId(departmentId)
                    .accessLevel(accessLevel != null ? accessLevel : 0)
                    .status("PENDING")
                    .createdBy(user.getId())
                    .build();

            documentRepository.save(document);

            // Publish async processing message
            documentProcessPublisher.publishDocumentProcess(document.getId());

            log.info("Document uploaded: {} (id={})", originalFilename, document.getId());

            return DocumentUploadResponse.builder()
                    .documentId(document.getId())
                    .documentName(originalFilename)
                    .status("PENDING")
                    .build();

        } catch (IOException e) {
            log.error("Failed to upload document: {}", e.getMessage());
            throw new BusinessException(500, "文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public PageResult<?> listDocuments(DocumentQueryRequest request, String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return PageResult.of(List.of(), 0, request.getPage(), request.getSize());
        }

        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Document> page;
        if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
            page = documentRepository.searchByKeyword(request.getKeyword(), pageable);
        } else if (request.getStatus() != null && !request.getStatus().isBlank()) {
            page = documentRepository.findByStatusAndDeletedFalse(request.getStatus(), pageable);
        } else {
            page = documentRepository.findByDeletedFalse(pageable);
        }

        return PageResult.of(page.getContent(), page.getTotalElements(),
                request.getPage(), request.getSize());
    }

    @Override
    @Transactional
    public void deleteDocument(Long documentId, String username) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new BusinessException(404, "文档不存在"));

        if (document.getDeleted()) {
            throw new BusinessException(400, "文档已删除");
        }

        // Soft delete
        document.setDeleted(true);
        documentRepository.save(document);

        // Delete file
        try {
            Files.deleteIfExists(Paths.get(document.getFilePath()));
        } catch (IOException e) {
            log.warn("Failed to delete file: {}", document.getFilePath());
        }

        // Delete chunks from DB
        documentChunkRepository.deleteByDocumentId(documentId);

        log.info("Document deleted: id={}", documentId);
    }

    @Override
    @Transactional
    public void processDocument(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new BusinessException(404, "文档不存在"));

        document.setStatus("PROCESSING");
        documentRepository.save(document);

        try {
            // Parse document
            DocumentParserService parserService = getParserService();
            String text = parserService.parseDocument(document.getFilePath(), document.getFileType());

            if (text == null || text.isBlank()) {
                document.setStatus("FAILED");
                document.setErrorMessage("文档内容为空");
                documentRepository.save(document);
                return;
            }

            // Split text
            TextSplitterService splitterService = getSplitterService();
            List<String> chunks = splitterService.splitText(text);

            // Save chunks to DB
            for (int i = 0; i < chunks.size(); i++) {
                DocumentChunk chunk = DocumentChunk.builder()
                        .documentId(documentId)
                        .chunkIndex(i)
                        .content(chunks.get(i))
                        .tokenCount(chunks.get(i).length())
                        .build();
                documentChunkRepository.save(chunk);
            }

            document.setChunkCount(chunks.size());
            document.setStatus("COMPLETED");
            documentRepository.save(document);

            log.info("Document processed: id={}, chunks={}", documentId, chunks.size());

        } catch (Exception e) {
            log.error("Document processing failed: id={}, error={}", documentId, e.getMessage());
            document.setStatus("FAILED");
            document.setErrorMessage(e.getMessage());
            documentRepository.save(document);
        }
    }

    // Helper methods for manual processing (used by MQ consumer)
    private DocumentParserService getParserService() {
        // Will be injected by Spring
        return new com.modeai.core.service.DocumentParserServiceImpl();
    }

    private TextSplitterService getSplitterService() {
        return new com.modeai.core.service.TextSplitterServiceImpl();
    }

    private String getFileExtension(String filename) {
        if (filename == null) return "";
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot + 1).toLowerCase() : "";
    }

    private boolean isSupportedFileType(String extension) {
        return List.of("pdf", "docx", "doc", "md", "txt").contains(extension.toLowerCase());
    }
}
