package com.modeai.core.service;

import com.modeai.common.dto.PageResult;
import com.modeai.core.domain.entity.ChatHistory;
import com.modeai.core.domain.entity.ChatMessage;
import com.modeai.core.domain.repository.ChatHistoryRepository;
import com.modeai.core.domain.repository.ChatMessageRepository;
import com.modeai.core.domain.repository.UserRepository;
import com.modeai.core.domain.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatHistoryService {

    private final ChatHistoryRepository chatHistoryRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long getOrCreateConversation(String conversationId, Long userId) {
        if (conversationId != null) {
            try {
                Long id = Long.parseLong(conversationId);
                return id;
            } catch (NumberFormatException e) {
                // ignore
            }
        }

        ChatHistory history = ChatHistory.builder()
                .userId(userId)
                .title("新对话")
                .messageCount(0)
                .build();
        chatHistoryRepository.save(history);
        return history.getId();
    }

    @Transactional
    public void saveUserMessage(String conversationId, Long userId, String content) {
        Long historyId = getOrCreateConversation(conversationId, userId);

        ChatMessage message = ChatMessage.builder()
                .historyId(historyId)
                .role("USER")
                .content(content)
                .tokenCount(content.length())
                .build();
        chatMessageRepository.save(message);

        // Update message count
        chatHistoryRepository.findById(historyId).ifPresent(history -> {
            history.setMessageCount(history.getMessageCount() + 1);
            history.setUpdatedAt(LocalDateTime.now());
            chatHistoryRepository.save(history);
        });
    }

    @Transactional
    public void saveAssistantMessage(String conversationId, Long userId, String content, List<String> sources) {
        Long historyId = getOrCreateConversation(conversationId, userId);

        ChatMessage message = ChatMessage.builder()
                .historyId(historyId)
                .role("ASSISTANT")
                .content(content)
                .tokenCount(content.length())
                .sources(sources != null ? String.join(",", sources) : null)
                .build();
        chatMessageRepository.save(message);

        // Update title from first user message
        chatHistoryRepository.findById(historyId).ifPresent(history -> {
            history.setMessageCount(history.getMessageCount() + 1);
            if ("新对话".equals(history.getTitle())) {
                List<ChatMessage> messages = chatMessageRepository.findByHistoryIdOrderByCreatedAtAsc(historyId);
                String firstUserMsg = messages.stream()
                        .filter(m -> "USER".equals(m.getRole()))
                        .map(m -> m.getContent().length() > 50 ? m.getContent().substring(0, 50) + "..." : m.getContent())
                        .findFirst()
                        .orElse("新对话");
                history.setTitle(firstUserMsg);
            }
            history.setUpdatedAt(LocalDateTime.now());
            chatHistoryRepository.save(history);
        });
    }

    public PageResult<ChatHistory> listHistories(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ChatHistory> result = chatHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return PageResult.of(result.getContent(), result.getTotalElements(), page, size);
    }

    public List<ChatMessage> getConversationMessages(Long historyId) {
        return chatMessageRepository.findByHistoryIdOrderByCreatedAtAsc(historyId);
    }
}
