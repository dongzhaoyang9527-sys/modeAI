package com.modeai.controller;

import com.modeai.api.dto.DocumentQueryRequest;
import com.modeai.api.dto.DocumentUploadResponse;
import com.modeai.common.dto.PageResult;
import com.modeai.common.dto.Result;
import com.modeai.core.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/upload")
    public Result<DocumentUploadResponse> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "departmentId", required = false) Long departmentId,
            @RequestParam(value = "accessLevel", required = false, defaultValue = "0") Integer accessLevel,
            Authentication authentication) {
        String username = authentication.getName();
        return Result.success(documentService.uploadDocument(file, departmentId, accessLevel, username));
    }

    @GetMapping
    public Result<PageResult<?>> listDocuments(
            @ModelAttribute DocumentQueryRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        return Result.success(documentService.listDocuments(request, username));
    }

    @GetMapping("/{documentId}")
    public Result<?> getDocument(@PathVariable Long documentId) {
        return Result.success(); // TODO: implement
    }

    @DeleteMapping("/{documentId}")
    public Result<Void> deleteDocument(@PathVariable Long documentId,
                                        Authentication authentication) {
        String username = authentication.getName();
        documentService.deleteDocument(documentId, username);
        return Result.success();
    }
}
