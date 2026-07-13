package com.enterprise.bff.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardViewDTO {
    private UserDTO user;
    private OrgDTO organization;
    private List<KpiSummaryDTO> kpis;
    private BigDecimal overallProgress;
}
