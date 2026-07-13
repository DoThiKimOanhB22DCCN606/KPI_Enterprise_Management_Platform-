package com.enterprise.kpi.infrastructure.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "kpi_values")
@Data
public class KpiValueEntity {

    @Id
    private UUID id;
    private UUID tenantId;
    private UUID kpiId;
    private java.time.OffsetDateTime periodStart;
    private java.time.OffsetDateTime periodEnd;
    private BigDecimal actualValue;
    private BigDecimal progressPercent;
    
    // String mapping for JSONB evidence column
    private String evidence;
    private String comment;
    private UUID createdBy;
    private Instant createdAt;
    private Instant updatedAt;

    @Version
    private Long version;
}
