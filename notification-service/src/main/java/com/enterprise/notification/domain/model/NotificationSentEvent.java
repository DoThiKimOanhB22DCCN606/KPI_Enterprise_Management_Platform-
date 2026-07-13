package com.enterprise.notification.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSentEvent {
    private String eventId;
    private UUID tenantId;
    private String sourceEventId;
    private NotificationChannel channel;
    private EscalationLevel escalationLevel;
    private String status; // "SUCCESS" or "FAILED"
    private long timestamp;
}
