package com.modeai.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.document.Document;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import com.modeai.common.util.DigestUtils;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagChatServiceImpl implements RagChatService {

    private final ChatClient.Builder chatClientBuilder;
    private final VectorStore vectorStore;
    private final DocumentPermissionService documentPermissionService;
    private final ChatHistoryService chatHistoryService;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String SYSTEM_PROMPT = """
            你是企业知识库智能助手。请基于以下参考资料回答用户问题。

            规则：
            1. 仅基于提供的参考资料回答，不要编造信息
            2. 如果参考资料中没有相关信息，请如实告知用户
            3. 回答要简洁、准确、专业
            4. 如果引用了参考资料，请注明来源
            5. 使用中文回答

            参考资料：
            %s
            """;

    private static final String CHAT_MEMORY_KEY = "chat:memory:";
    private static final String CHAT_CACHE_KEY = "chat:cache:";
    private static final int MEMORY_MAX_MESSAGES = 20;
    private static final int CACHE_TTL_HOURS = 24;

    @Override
    public Flux<String> streamChat(String userInput, String conversationId, String username) {
        log.info("Stream chat request: user={}, conversationId={}", username, conversationId);

        // Check cache first
        String cacheKey = CHAT_CACHE_KEY + DigestUtils.md5Hex(userInput);
        String cachedAnswer = (String) redisTemplate.opsForValue().get(cacheKey);
        if (cachedAnswer != null) {
            log.info("Cache hit for query: {}", userInput);
            return Flux.just(cachedAnswer);
        }

        // Get user info
        Long userId = getUserId(username);
        if (userId == null) {
            return Flux.just("用户信息获取失败，请重新登录。");
        }

        // Retrieve relevant documents
        List<Long> accessibleIds = documentPermissionService.getAccessibleDocumentIds(userId, "READ");
        List<Document> relevantDocs = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(userInput)
                        .topK(5)
                        .similarityThreshold(0.7)
                        .build());

        // Filter by permissions
        relevantDocs = relevantDocs.stream()
                .filter(doc -> {
                    Object docId = doc.getMetadata().get("document_id");
                    if (docId == null) return false;
                    Long id = docId instanceof Number ? ((Number) docId).longValue() : Long.parseLong(docId.toString());
                    return accessibleIds.contains(id);
                })
                .toList();

        // Build context
        String context = relevantDocs.stream()
                .map(doc -> {
                    String title = (String) doc.getMetadata().getOrDefault("document_title", "未知文档");
                    return "【" + title + "】\n" + doc.getContent();
                })
                .collect(Collectors.joining("\n\n---\n\n"));

        // Get conversation history from Redis
        List<Message> historyMessages = getConversationHistory(conversationId);

        // Build prompt
        String systemContent = String.format(SYSTEM_PROMPT, context.isEmpty() ? "暂无相关参考资料" : context);
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(systemContent));
        messages.addAll(historyMessages);
        messages.add(new UserMessage(userInput));

        // Save user message
        chatHistoryService.saveUserMessage(conversationId, userId, userInput);

        // Stream response
        StringBuilder fullAnswer = new StringBuilder();
        ChatClient chatClient = chatClientBuilder.build();

        return chatClient.prompt()
                .messages(messages)
                .stream()
                .content()
                .doOnNext(fullAnswer::append)
                .doOnComplete(() -> {
                    // Save assistant message
                    chatHistoryService.saveAssistantMessage(conversationId, userId, fullAnswer.toString(),
                            relevantDocs.stream().map(d -> (String) d.getMetadata().get("document_title")).toList());
                    // Update conversation memory
                    updateConversationMemory(conversationId, userInput, fullAnswer.toString());
                    // Cache the answer
                    redisTemplate.opsForValue().set(cacheKey, fullAnswer.toString(),
                            CACHE_TTL_HOURS, TimeUnit.HOURS);
                });
    }

    @Override
    public String chat(String userInput, String conversationId, String username) {
        StringBuilder answer = new StringBuilder();
        streamChat(userInput, conversationId, username)
                .doOnNext(answer::append)
                .blockLast(Duration.ofSeconds(60));
        return answer.toString();
    }

    private List<Message> getConversationHistory(String conversationId) {
        String key = CHAT_MEMORY_KEY + conversationId;
        List<Map<String, String>> history = (List<Map<String, String>>) redisTemplate.opsForValue().get(key);
        if (history == null || history.isEmpty()) {
            return List.of();
        }

        return history.stream()
                .map(msg -> {
                    if ("USER".equals(msg.get("role"))) {
                        return (Message) new UserMessage(msg.get("content"));
                    } else {
                        return (Message) new SystemMessage(msg.get("content"));
                    }
                })
                .toList();
    }

    private void updateConversationMemory(String conversationId, String userMessage, String assistantMessage) {
        String key = CHAT_MEMORY_KEY + conversationId;
        List<Map<String, String>> history = (List<Map<String, String>>) redisTemplate.opsForValue().get(key);
        if (history == null) {
            history = new ArrayList<>();
        }

        history.add(Map.of("role", "USER", "content", userMessage));
        history.add(Map.of("role", "ASSISTANT", "content", assistantMessage));

        // Keep only last N messages
        if (history.size() > MEMORY_MAX_MESSAGES * 2) {
            history = history.subList(history.size() - MEMORY_MAX_MESSAGES * 2, history.size());
        }

        redisTemplate.opsForValue().set(key, history, 30, TimeUnit.MINUTES);
    }

    private Long getUserId(String username) {
        // Simplified - in production, use repository
        return 1L;
    }
}
