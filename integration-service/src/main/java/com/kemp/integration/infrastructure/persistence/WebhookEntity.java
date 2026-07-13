package com.kemp.integration.infrastructure.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "webhook_subscriptions")
@Data
public class WebhookEntity {
    @Id
    private UUID id;
    private UUID tenantId;
    private String url;
    
    @JdbcTypeCode(SqlTypes.ARRAY)
    private List<String> events;
    
    private String secretHash;
    private Boolean active;
}
