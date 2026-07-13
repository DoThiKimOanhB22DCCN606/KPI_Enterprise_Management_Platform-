package com.kemp.dashboard.infrastructure.persistence;

import com.kemp.dashboard.domain.model.WidgetType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "dashboard_widgets")
@Data
public class DashboardWidgetEntity {
    @Id
    private UUID id;
    private UUID dashboardId;
    
    @Enumerated(EnumType.STRING)
    private WidgetType widgetType;
    
    private String title;
    private Integer x;
    private Integer y;
    private Integer width;
    private Integer height;
    
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> configJson;
}
