package com.enterprise.notification.domain.port;

import com.enterprise.notification.domain.model.EscalationLevel;
import com.enterprise.notification.domain.model.NotificationChannel;

import java.util.UUID;

public interface NotificationEventPublisherPort {
    
    void publishNotificationSentEvent(
            String eventId, 
            UUID tenantId, 
            String sourceEventId, 
            NotificationChannel channel, 
            EscalationLevel escalationLevel, 
            boolean success
    );
}
