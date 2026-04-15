package com.modeai.core.domain.repository;

import com.modeai.core.domain.entity.ChatHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Long> {
    Page<ChatHistory> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
