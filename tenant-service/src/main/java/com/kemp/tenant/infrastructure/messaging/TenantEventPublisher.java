package com.kemp.tenant.infrastructure.messaging;

import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TenantEventPublisher {
    private final RabbitTemplate rabbitTemplate;

    public void publishTenantCreated(UUID tenantId, String code) {
        rabbitTemplate.convertAndSend("tenant.exchange", "tenant.created", 
            Map.of("tenantId", tenantId, "code", code, "event", "TenantCreated"));
    }
}
