package com.modeai.infra.ai;

import com.modeai.core.domain.entity.TokenUsage;
import com.modeai.core.domain.repository.TokenUsageRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenUsageTracker {

    private final TokenUsageRepository tokenUsageRepository;
    private final MeterRegistry meterRegistry;

    private Counter totalTokenCounter;
    private Counter chatRequestCounter;

    public void recordUsage(Long userId, String model, int promptTokens,
                             int completionTokens, String requestType) {
        int totalTokens = promptTokens + completionTokens;

        // Save to database
        TokenUsage usage = TokenUsage.builder()
                .userId(userId)
                .model(model)
                .promptTokens(promptTokens)
                .completionTokens(completionTokens)
                .totalTokens(totalTokens)
                .requestType(requestType)
                .build();
        tokenUsageRepository.save(usage);

        // Record metrics
        meterRegistry.counter("token.usage.total",
                "model", model, "type", requestType)
                .increment(totalTokens);

        meterRegistry.counter("token.usage.prompt",
                "model", model)
                .increment(promptTokens);

        meterRegistry.counter("token.usage.completion",
                "model", model)
                .increment(completionTokens);

        log.info("Token usage recorded: userId={}, model={}, prompt={}, completion={}, total={}",
                userId, model, promptTokens, completionTokens, totalTokens);
    }
}
