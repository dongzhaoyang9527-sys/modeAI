package com.modeai.core.service;

import com.modeai.api.dto.DocumentQueryRequest;
import com.modeai.api.dto.DocumentUploadResponse;
import com.modeai.common.dto.PageResult;
import org.springframework.web.multipart.MultipartFile;

public interface DocumentService {
    DocumentUploadResponse uploadDocument(MultipartFile file, Long departmentId, Integer accessLevel, String username);
    PageResult<?> listDocuments(DocumentQueryRequest request, String username);
    void deleteDocument(Long documentId, String username);
    void processDocument(Long documentId);
}
