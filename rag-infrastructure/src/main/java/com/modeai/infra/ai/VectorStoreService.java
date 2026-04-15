package com.modeai.infra.ai;

import com.modeai.core.domain.entity.Document;
import com.modeai.core.domain.entity.DocumentChunk;
import com.modeai.core.domain.repository.DocumentChunkRepository;
import com.modeai.core.domain.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class VectorStoreService {

    private final VectorStore vectorStore;
    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository documentChunkRepository;

    public void indexDocument(Long documentId) {
        Document doc = documentRepository.findById(documentId).orElse(null);
        if (doc == null) {
            log.warn("Document not found: {}", documentId);
            return;
        }

        List<DocumentChunk> chunks = documentChunkRepository.findByDocumentIdOrderByChunkIndex(documentId);
        if (chunks.isEmpty()) {
            log.warn("No chunks found for document: {}", documentId);
            return;
        }

        List<org.springframework.ai.document.Document> aiDocuments = chunks.stream().map(chunk -> {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("document_id", documentId);
            metadata.put("department_id", doc.getDepartmentId() != null ? doc.getDepartmentId() : 0);
            metadata.put("access_level", doc.getAccessLevel());
            metadata.put("chunk_index", chunk.getChunkIndex());
            metadata.put("document_title", doc.getTitle());

            return new org.springframework.ai.document.Document(chunk.getContent(), metadata);
        }).toList();

        vectorStore.add(aiDocuments);
        log.info("Indexed {} chunks for document: {}", aiDocuments.size(), documentId);
    }

    public void removeDocument(Long documentId) {
        // Note: Spring AI VectorStore doesn't have a direct delete by metadata
        // In production, use native Milvus client for bulk delete
        log.info("Remove document from vector store: {}", documentId);
    }

    public List<org.springframework.ai.document.Document> similaritySearch(String query, int topK, List<Long> accessibleDocumentIds) {
        if (accessibleDocumentIds == null || accessibleDocumentIds.isEmpty()) {
            return List.of();
        }

        String idList = accessibleDocumentIds.stream()
                .map(String::valueOf)
                .reduce((a, b) -> a + "," + b)
                .orElse("");

        // Use metadata filter for permission control
        org.springframework.ai.vectorstore.SearchRequest searchRequest =
                org.springframework.ai.vectorstore.SearchRequest.builder()
                        .query(query)
                        .topK(topK)
                        .similarityThreshold(0.7)
                        .build();

        // For Milvus-specific filtering, we would use native expression
        // This is a simplified version
        List<org.springframework.ai.document.Document> results = vectorStore.similaritySearch(searchRequest);

        // Filter by accessible document IDs
        return results.stream()
                .filter(doc -> {
                    Object docId = doc.getMetadata().get("document_id");
                    if (docId == null) return false;
                    Long id = docId instanceof Number ? ((Number) docId).longValue() : Long.parseLong(docId.toString());
                    return accessibleDocumentIds.contains(id);
                })
                .toList();
    }
}
