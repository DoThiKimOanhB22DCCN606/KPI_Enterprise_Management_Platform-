package com.kemp.tenant.infrastructure.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "tenant_subscriptions")
@Data
public class TenantSubscriptionEntity {
    @Id
    private UUID id;
    private UUID tenantId;
    private String planType;
    private Integer maxUsers;
    private Integer maxKpis;
    private OffsetDateTime expiresAt;
    @CreationTimestamp
    private OffsetDateTime createdAt;
    @UpdateTimestamp
    private OffsetDateTime updatedAt;
}
