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
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "alert_rules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertRuleEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** References the KPI this rule monitors */
    private UUID kpiId;

    /** e.g. "THRESHOLD", "TREND", "ANOMALY" */
    private String conditionType;

    @Column(precision = 20, scale = 4)
    private BigDecimal thresholdValue;

    /** e.g. "LESS_THAN", "GREATER_THAN", "EQUALS" */
    @Column(nullable = false)
    @Builder.Default
    private String comparisonOperator = "LESS_THAN";

    /** INFO | WARNING | CRITICAL */
    @Column(nullable = false)
    @Builder.Default
    private String severity = "WARNING";

    /** IN_APP | EMAIL | SLACK | TELEGRAM */
    @Column(nullable = false)
    @Builder.Default
    private String notificationChannel = "IN_APP";

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    private UUID createdBy;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private OffsetDateTime updatedAt;
}
