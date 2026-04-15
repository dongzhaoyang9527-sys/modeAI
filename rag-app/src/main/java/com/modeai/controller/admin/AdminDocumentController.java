package com.modeai.controller.admin;

import com.modeai.common.dto.PageResult;
import com.modeai.common.dto.Result;
import com.modeai.core.domain.entity.Document;
import com.modeai.core.domain.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/documents")
@RequiredArgsConstructor
public class AdminDocumentController {

    private final DocumentRepository documentRepository;

    @GetMapping
    public Result<PageResult<Document>> listAllDocuments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Document> result = documentRepository.findByDeletedFalse(pageable);
        return Result.success(PageResult.of(result.getContent(), result.getTotalElements(), page, size));
    }

    @GetMapping("/stats")
    public Result<?> getDocumentStats() {
        long total = documentRepository.count();
        long completed = documentRepository.countByStatusAndDeletedFalse("COMPLETED");
        long processing = documentRepository.countByStatusAndDeletedFalse("PROCESSING");
        long failed = documentRepository.countByStatusAndDeletedFalse("FAILED");

        return Result.success(java.util.Map.of(
                "total", total,
                "completed", completed,
                "processing", processing,
                "failed", failed
        ));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteDocument(@PathVariable Long id) {
        Document doc = documentRepository.findById(id).orElse(null);
        if (doc != null) {
            doc.setDeleted(true);
            documentRepository.save(doc);
        }
        return Result.success();
    }
}
