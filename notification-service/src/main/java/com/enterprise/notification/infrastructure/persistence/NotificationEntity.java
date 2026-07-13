package com.enterprise.notification.infrastructure.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Data
public class NotificationEntity {
    @Id
    private UUID id;
    private UUID userId;
    private String channel;
    private String message;
    private String status;
    private Instant sentAt;
    private Instant readAt;
    private Instant createdAt;
}
