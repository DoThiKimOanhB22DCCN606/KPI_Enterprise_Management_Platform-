package com.kemp.dashboard.infrastructure.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "dashboards")
@Data
public class DashboardEntity {
    @Id
    private UUID id;
    private UUID tenantId;
    private String name;
    private String description;
    @JdbcTypeCode(SqlTypes.UUID)
    private String publicToken;
    private Boolean fullscreenEnabled;
    private Boolean autoRotate;
    private Integer autoRotateInterval;
    private UUID createdBy;
    @JdbcTypeCode(SqlTypes.JSON)
    private String layoutJson;
}
