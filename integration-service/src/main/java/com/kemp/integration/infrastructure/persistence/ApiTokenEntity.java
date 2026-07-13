package com.kemp.integration.infrastructure.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Entity
@Table(name = "api_tokens")
@Data
public class ApiTokenEntity {
    @Id
    private UUID id;
    private UUID tenantId;
    private UUID userId;
    private String tokenHash;
    private String name;
    private LocalDateTime expiredAt;
}
