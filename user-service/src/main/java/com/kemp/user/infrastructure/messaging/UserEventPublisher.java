package com.kemp.user.infrastructure.messaging;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserEventPublisher {
    private final RabbitTemplate rabbitTemplate;

    public void publishUserCreated(UUID tenantId, UUID userId, String email, String roleCode) {
        Map<String, Object> payload = Map.of(
            "eventId", UUID.randomUUID().toString(),
            "timestamp", OffsetDateTime.now().toString(),
            "tenantId", tenantId.toString(),
            "userId", userId.toString(),
            "email", email,
            "role", roleCode != null ? roleCode : ""
        );
        rabbitTemplate.convertAndSend("kemp.events", "user.created", payload);
    }
}
