package com.modeai.infra.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String DOCUMENT_PROCESS_QUEUE = "document.process";
    public static final String DOCUMENT_PROCESS_EXCHANGE = "document.process.exchange";
    public static final String DOCUMENT_PROCESS_ROUTING_KEY = "document.process";

    @Bean
    public Queue documentProcessQueue() {
        return QueueBuilder.durable(DOCUMENT_PROCESS_QUEUE)
                .withArgument("x-dead-letter-exchange", "document.dlx.exchange")
                .withArgument("x-dead-letter-routing-key", "document.dlx")
                .build();
    }

    @Bean
    public DirectExchange documentProcessExchange() {
        return new DirectExchange(DOCUMENT_PROCESS_EXCHANGE);
    }

    @Bean
    public Binding documentProcessBinding() {
        return BindingBuilder.bind(documentProcessQueue())
                .to(documentProcessExchange())
                .with(DOCUMENT_PROCESS_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
