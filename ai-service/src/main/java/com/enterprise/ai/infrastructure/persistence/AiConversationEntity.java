package com.enterprise.ai.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ai_conversations")
@Data
public class AiConversationEntity {
    @Id
    private UUID id;
    private UUID userId;
    private UUID tenantId;
    private Instant createdAt;
    private Instant updatedAt;
}
