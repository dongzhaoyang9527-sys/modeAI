package com.modeai.infra.mq;

import com.modeai.core.service.DocumentService;
import com.modeai.infra.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentProcessConsumer {

    private final DocumentService documentService;

    @RabbitListener(queues = RabbitMQConfig.DOCUMENT_PROCESS_QUEUE)
    public void handleDocumentProcess(Map<String, Object> message) {
        try {
            Long documentId = ((Number) message.get("documentId")).longValue();
            log.info("Received document process message: documentId={}", documentId);
            documentService.processDocument(documentId);
        } catch (Exception e) {
            log.error("Failed to process document: {}", e.getMessage(), e);
        }
    }
}
