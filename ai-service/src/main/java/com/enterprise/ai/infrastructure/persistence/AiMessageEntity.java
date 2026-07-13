package com.enterprise.ai.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ai_messages")
@Data
public class AiMessageEntity {
    @Id
    private UUID id;
    private UUID conversationId;
    private String role; // USER, AI
    @Column(columnDefinition="TEXT")
    private String content;
    @Column(columnDefinition="TEXT")
    private String sqlQuery;
    private Instant createdAt;
}
