package com.enterprise.kpi.infrastructure.persistence;

import com.enterprise.kpi.domain.model.KpiStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "kpis")
@SQLDelete(sql = "UPDATE kpis SET deleted_at = CURRENT_TIMESTAMP WHERE id=?")
@SQLRestriction("deleted_at IS NULL")
@Data
public class KpiEntity {
    @Id
    private UUID id;
    private UUID tenantId;
    private UUID templateId;
    private UUID ownerId;
    private String name;
    private String frequency;
    
    @Enumerated(EnumType.STRING)
    private KpiStatus status;
    
    private BigDecimal target;
    private BigDecimal currentProgress;
    private String formula;
    
    @Column(name = "evaluation_score")
    private BigDecimal evaluationScore;
    @Column(name = "manager_comments")
    private String managerComments;
    
    @Column(name = "cycle_id")
    private UUID cycleId;

    private LocalDate startDate;
    private LocalDate endDate;
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
