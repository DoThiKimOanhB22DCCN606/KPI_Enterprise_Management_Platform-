package com.enterprise.kpi.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "kpi_templates")
@SQLDelete(sql = "UPDATE kpi_templates SET deleted_at = CURRENT_TIMESTAMP WHERE id=?")
@SQLRestriction("deleted_at IS NULL")
@Data
public class KpiTemplateEntity {
    @Id
    private UUID id;
    private UUID tenantId;
    private String name;
    private String description;
    private String category;
    private String defaultFrequency;
    private BigDecimal defaultTarget;
    private String defaultFormula;

    @Column(name = "deleted_at")
    private java.time.Instant deletedAt;
}
