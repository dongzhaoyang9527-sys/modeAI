package com.modeai.core.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "kb_document")
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(nullable = false, length = 500)
    private String fileName;

    @Column(nullable = false, length = 1000)
    private String filePath;

    @Builder.Default
    private Long fileSize = 0L;

    private String fileType;

    private String contentHash;

    private Long departmentId;

    @Builder.Default
    private Integer accessLevel = 0;

    @Builder.Default
    @Column(length = 20)
    private String status = "PENDING";

    private String errorMessage;

    @Builder.Default
    private Integer chunkCount = 0;

    private Long createdBy;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    @Builder.Default
    private Boolean deleted = false;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
