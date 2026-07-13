package com.enterprise.alert.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "alert_instances")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertInstanceEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID tenantId;

    /** The AlertRuleEntity that triggered this instance */
    private UUID ruleId;

    /** The KPI whose value breached the threshold */
    private UUID kpiId;

    @Column(precision = 20, scale = 4)
    private BigDecimal triggeredValue;

    @Column(precision = 20, scale = 4)
    private BigDecimal thresholdValue;

    /** INFO | WARNING | CRITICAL */
    private String severity;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false)
    @Builder.Default
    private boolean resolved = false;

    private OffsetDateTime resolvedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
