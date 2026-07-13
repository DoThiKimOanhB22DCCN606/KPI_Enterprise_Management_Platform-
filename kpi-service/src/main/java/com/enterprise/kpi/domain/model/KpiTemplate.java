package com.enterprise.kpi.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KpiTemplate {
    private UUID id;
    private UUID tenantId;
    private String name;
    private String description;
    private String category;
    private String defaultFrequency;
    private BigDecimal defaultTarget;
    private String defaultFormula;

    public KpiTemplate cloneTemplate() {
        return KpiTemplate.builder()
                .id(UUID.randomUUID())
                .tenantId(this.tenantId)
                .name(this.name + " - Copy")
                .description(this.description)
                .category(this.category)
                .defaultFrequency(this.defaultFrequency)
                .defaultTarget(this.defaultTarget)
                .defaultFormula(this.defaultFormula)
                .build();
    }
}
