package com.modeai.core.domain.repository;

import com.modeai.core.domain.entity.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    Page<Document> findByCreatedByAndDeletedFalse(Long createdBy, Pageable pageable);
    Page<Document> findByDepartmentIdAndDeletedFalse(Long departmentId, Pageable pageable);
    Page<Document> findByStatusAndDeletedFalse(String status, Pageable pageable);
    Page<Document> findByDeletedFalse(Pageable pageable);

    @Query("SELECT d FROM Document d WHERE d.deleted = false AND " +
           "(d.title LIKE %:keyword% OR d.fileName LIKE %:keyword%)")
    Page<Document> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    long countByCreatedByAndDeletedFalse(Long createdBy);
    long countByStatusAndDeletedFalse(String status);
}
