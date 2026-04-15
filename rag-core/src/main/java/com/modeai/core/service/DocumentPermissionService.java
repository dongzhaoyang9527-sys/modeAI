package com.modeai.core.service;

import com.modeai.core.domain.entity.Document;
import com.modeai.core.domain.entity.User;
import com.modeai.core.domain.repository.DocumentPermissionRepository;
import com.modeai.core.domain.repository.DocumentRepository;
import com.modeai.core.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentPermissionService {

    private final DocumentPermissionRepository permissionRepository;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;

    public boolean canAccessDocument(Long userId, Long documentId, String permissionType) {
        Document doc = documentRepository.findById(documentId).orElse(null);
        if (doc == null || doc.getDeleted()) {
            return false;
        }

        // Public documents accessible by all
        if (doc.getAccessLevel() == 0 && "READ".equals(permissionType)) {
            return true;
        }

        // Creator has full access
        if (doc.getCreatedBy() != null && doc.getCreatedBy().equals(userId)) {
            return true;
        }

        // Check ACL
        boolean hasPermission = permissionRepository.existsByDocumentIdAndUserIdAndPermissionType(
                documentId, userId, permissionType);
        if (hasPermission) return true;

        // Check role permissions
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            List<Long> roleIds = user.getRoles().stream()
                    .map(role -> role.getId())
                    .collect(Collectors.toList());

            if (!roleIds.isEmpty()) {
                List<Long> accessibleIds = permissionRepository.findAccessibleDocumentIds(
                        userId, roleIds, permissionType);
                if (accessibleIds.contains(documentId)) {
                    return true;
                }
            }

            // Check department access
            if (doc.getDepartmentId() != null) {
                boolean inSameDepartment = user.getDepartments().stream()
                        .anyMatch(dept -> dept.getId().equals(doc.getDepartmentId()));
                if (inSameDepartment && doc.getAccessLevel() <= 1) {
                    return true;
                }
            }
        }

        return false;
    }

    public List<Long> getAccessibleDocumentIds(Long userId, String permissionType) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return List.of();
        }

        List<Long> roleIds = user.getRoles().stream()
                .map(role -> role.getId())
                .collect(Collectors.toList());

        List<Long> aclIds = permissionRepository.findAccessibleDocumentIds(
                userId, roleIds, permissionType);

        // Add public documents
        List<Long> publicIds = documentRepository.findByDeletedFalse(
                org.springframework.data.domain.Pageable.unpaged()).getContent().stream()
                .filter(doc -> doc.getAccessLevel() == 0)
                .map(Document::getId)
                .collect(Collectors.toList());

        // Add department documents
        List<Long> deptIds = user.getDepartments().stream()
                .flatMap(dept -> documentRepository.findByDepartmentIdAndDeletedFalse(
                        dept.getId(), org.springframework.data.domain.Pageable.unpaged()).getContent().stream())
                .filter(doc -> doc.getAccessLevel() <= 1)
                .map(Document::getId)
                .collect(Collectors.toList());

        // Merge all
        List<Long> allIds = new java.util.ArrayList<>(aclIds);
        allIds.addAll(publicIds);
        allIds.addAll(deptIds);

        // Add user's own documents
        allIds.addAll(documentRepository.findByCreatedByAndDeletedFalse(
                userId, org.springframework.data.domain.Pageable.unpaged()).getContent().stream()
                .map(Document::getId)
                .collect(Collectors.toList()));

        return allIds.stream().distinct().collect(Collectors.toList());
    }
}
