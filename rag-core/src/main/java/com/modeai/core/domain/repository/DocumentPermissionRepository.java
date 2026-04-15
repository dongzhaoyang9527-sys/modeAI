package com.modeai.core.domain.repository;

import com.modeai.core.domain.entity.DocumentPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentPermissionRepository extends JpaRepository<DocumentPermission, Long> {
    List<DocumentPermission> findByDocumentId(Long documentId);

    boolean existsByDocumentIdAndUserIdAndPermissionType(Long documentId, Long userId, String permissionType);

    @Query("SELECT dp.documentId FROM DocumentPermission dp WHERE " +
           "(dp.userId = :userId OR dp.roleId IN :roleIds) AND dp.permissionType = :permissionType")
    List<Long> findAccessibleDocumentIds(@Param("userId") Long userId,
                                          @Param("roleIds") List<Long> roleIds,
                                          @Param("permissionType") String permissionType);
}
