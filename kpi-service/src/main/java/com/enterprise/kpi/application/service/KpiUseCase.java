package com.enterprise.kpi.application.service;

import com.enterprise.kpi.application.dto.KpiCreateRequest;
import com.enterprise.kpi.application.dto.KpiDTO;
import com.enterprise.kpi.application.dto.KpiProgressRecordRequest;
import com.enterprise.kpi.application.dto.KpiFormulaUpdateRequest;
import com.enterprise.kpi.application.dto.KpiCalculateRequest;
import com.enterprise.kpi.application.dto.KpiApprovalRequest;

import java.util.UUID;

public interface KpiUseCase {
    KpiDTO getKpiById(UUID id);
    KpiDTO createKpi(KpiCreateRequest request);
    KpiDTO recordKpiProgress(UUID id, KpiProgressRecordRequest request);
    KpiDTO updateFormula(UUID id, KpiFormulaUpdateRequest request);
    KpiDTO calculateKpi(UUID id, KpiCalculateRequest request);
    KpiDTO processApproval(UUID id, KpiApprovalRequest request, UUID currentUserId);
    KpiDTO completeKpi(UUID id, com.enterprise.kpi.application.dto.KpiCompleteRequest request);
    void deleteKpi(UUID id);
    org.springframework.data.domain.Page<KpiDTO> getKpis(org.springframework.data.domain.Pageable pageable, String status, UUID ownerId);
}
