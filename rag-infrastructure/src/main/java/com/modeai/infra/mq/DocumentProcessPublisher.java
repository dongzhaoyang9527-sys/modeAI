package com.modeai.infra.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modeai.infra.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentProcessPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public void publishDocumentProcess(Long documentId) {
        try {
            Map<String, Object> message = Map.of(
                    "documentId", documentId,
                    "timestamp", System.currentTimeMillis()
            );
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.DOCUMENT_PROCESS_EXCHANGE,
                    RabbitMQConfig.DOCUMENT_PROCESS_ROUTING_KEY,
                    message
            );
            log.info("Published document process message: documentId={}", documentId);
        } catch (Exception e) {
            log.error("Failed to publish document process message: {}", e.getMessage());
        }
    }
}
