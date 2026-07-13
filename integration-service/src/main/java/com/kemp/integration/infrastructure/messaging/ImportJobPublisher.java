package com.kemp.integration.infrastructure.messaging;

import com.kemp.integration.infrastructure.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ImportJobPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publish(ImportJobMessage message) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.IMPORT_QUEUE, message);
    }
}
