package com.modeai.core.domain.repository;

import com.modeai.core.domain.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByHistoryIdOrderByCreatedAtAsc(Long historyId);
}
