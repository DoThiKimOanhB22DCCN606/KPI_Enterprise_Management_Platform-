package com.enterprise.notification.infrastructure.adapter.channel;

import com.enterprise.notification.infrastructure.persistence.NotificationEntity;
import com.enterprise.notification.infrastructure.persistence.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class InAppNotificationAdapter {

    private final NotificationRepository notificationRepository;

    public void send(UUID userId, String message) {
        NotificationEntity notification = new NotificationEntity();
        notification.setId(UUID.randomUUID());
        notification.setUserId(userId);
        notification.setChannel("IN_APP");
        notification.setMessage(message);
        notification.setStatus("SENT");
        notification.setCreatedAt(Instant.now());
        notification.setSentAt(Instant.now());
        
        notificationRepository.save(notification);
        log.info("In-App notification saved for user {}", userId);
    }
}
