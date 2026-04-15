package com.modeai.core.service;

import reactor.core.publisher.Flux;

public interface RagChatService {
    Flux<String> streamChat(String userInput, String conversationId, String username);
    String chat(String userInput, String conversationId, String username);
}
