package com.modeai.controller;

import com.modeai.api.dto.ChatRequest;
import com.modeai.common.dto.PageResult;
import com.modeai.common.dto.Result;
import com.modeai.core.domain.entity.ChatHistory;
import com.modeai.core.domain.entity.ChatMessage;
import com.modeai.core.service.ChatHistoryService;
import com.modeai.core.service.RagChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final RagChatService ragChatService;
    private final ChatHistoryService chatHistoryService;

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@RequestBody ChatRequest request,
                                    Authentication authentication) {
        String username = authentication.getName();
        return ragChatService.streamChat(request.getMessage(), request.getConversationId(), username);
    }

    @PostMapping
    public Result<String> chat(@RequestBody ChatRequest request,
                                Authentication authentication) {
        String username = authentication.getName();
        String answer = ragChatService.chat(request.getMessage(), request.getConversationId(), username);
        return Result.success(answer);
    }

    @GetMapping("/histories")
    public Result<PageResult<ChatHistory>> listHistories(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        String username = authentication.getName();
        // TODO: get userId from username
        return Result.success(chatHistoryService.listHistories(1L, page, size));
    }

    @GetMapping("/histories/{historyId}/messages")
    public Result<List<ChatMessage>> getMessages(@PathVariable Long historyId) {
        return Result.success(chatHistoryService.getConversationMessages(historyId));
    }

    @DeleteMapping("/histories/{historyId}")
    public Result<Void> deleteHistory(@PathVariable Long historyId) {
        // TODO: implement delete
        return Result.success();
    }
}
