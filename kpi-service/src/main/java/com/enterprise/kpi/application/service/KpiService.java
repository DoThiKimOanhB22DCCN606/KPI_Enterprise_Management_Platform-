package com.enterprise.kpi.application.service;

import com.enterprise.kpi.application.dto.KpiCreateRequest;
import com.enterprise.kpi.application.dto.KpiDTO;
import com.enterprise.kpi.application.dto.KpiFormulaUpdateRequest;
import com.enterprise.kpi.application.dto.KpiCalculateRequest;
import com.enterprise.kpi.application.dto.KpiProgressRecordRequest;
import com.enterprise.kpi.domain.exception.BusinessRuleException;
import com.enterprise.kpi.domain.model.KpiStatus;
import com.enterprise.kpi.domain.model.KPI;
import com.enterprise.kpi.application.dto.KpiApprovalRequest;
import com.enterprise.kpi.domain.model.ApprovalAction;
import com.enterprise.kpi.application.service.port.OrganizationClientPort;
import com.enterprise.kpi.infrastructure.config.TenantContext;
import com.enterprise.kpi.infrastructure.persistence.KpiEntity;
import com.enterprise.kpi.infrastructure.persistence.KpiRepository;
import com.enterprise.kpi.infrastructure.persistence.KpiTemplateEntity;
import com.enterprise.kpi.infrastructure.persistence.KpiTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;
import java.util.List;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.stream.Collectors;
import com.enterprise.kpi.infrastructure.persistence.KpiValueEntity;
import com.enterprise.kpi.infrastructure.persistence.KpiValueRepository;
import com.enterprise.kpi.application.dto.KpiValueDTO;

@Service
@RequiredArgsConstructor
public class KpiService implements KpiUseCase {

    private final KpiRepository kpiRepository;
    private final KpiTemplateRepository kpiTemplateRepository;
    private final KpiValueRepository kpiValueRepository;
    private final KpiAttachmentService kpiAttachmentService;
    private final FormulaEvaluatorService evaluatorService;
    private final OrganizationClientPort organizationClientPort;

    @Override
    @Transactional
    public KpiDTO createKpi(KpiCreateRequest request) {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessRuleException("ERR_TENANT_CONTEXT_MISSING", "Tenant context is required");
        }

        KpiTemplateEntity template = kpiTemplateRepository.findByIdAndTenantId(request.getTemplateId(), tenantId)
                .orElseThrow(() -> new BusinessRuleException("ERR_TEMPLATE_NOT_FOUND", "KPI Template not found"));
        
        UUID id = UUID.randomUUID();
        KPI kpi = KPI.create(id, tenantId, request.getTemplateId(), request.getOwnerId(), template.getName(), request.getFrequency(), request.getTarget(), request.getFormula(), request.getCycleId());

        KpiEntity entity = new KpiEntity();
        entity.setId(kpi.getId());
        entity.setTenantId(kpi.getTenantId());
        entity.setTemplateId(kpi.getTemplateId());
        entity.setOwnerId(kpi.getOwnerId());
        entity.setName(kpi.getName());
        entity.setFrequency(kpi.getFrequency());

        KpiStatus initialStatus = kpi.getStatus(); // defaults to DRAFT
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UUID) {
            UUID currentUserId = (UUID) auth.getPrincipal();
            if (!currentUserId.equals(request.getOwnerId())) {
                initialStatus = KpiStatus.ACTIVE;
            }
        }
        entity.setStatus(initialStatus);

        entity.setTarget(kpi.getTarget());
        entity.setCurrentProgress(kpi.getCurrentProgress());
        entity.setFormula(kpi.getFormula());
        entity.setCycleId(kpi.getCycleId());

        entity = kpiRepository.save(entity);
        return mapToDTO(entity, false);
    }

    @Override
    @Transactional(readOnly = true)
    public KpiDTO getKpiById(UUID id) {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessRuleException("ERR_TENANT_CONTEXT_MISSING", "Tenant context is required");
        }

        KpiEntity entity = kpiRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("KPI not found"));

        return mapToDTO(entity, true);
    }

    @Override
    @Transactional
    public KpiDTO recordKpiProgress(UUID id, KpiProgressRecordRequest request) {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessRuleException("ERR_TENANT_CONTEXT_MISSING", "Tenant context is required");
        }
        
        KpiEntity entity = kpiRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("KPI not found"));

        if (entity.getStatus() == KpiStatus.CLOSED || entity.getStatus() == KpiStatus.ARCHIVED) {
            throw new BusinessRuleException("ERR_KPI_IMMUTABLE", "Cannot modify values of a CLOSED or ARCHIVED KPI");
        }
        
        KPI kpi = mapToDomain(entity);
        kpi.updateProgress(request.getValue());
        
        entity.setCurrentProgress(kpi.getCurrentProgress());
        entity = kpiRepository.save(entity);
        
        KpiValueEntity valueEntity = new KpiValueEntity();
        valueEntity.setId(request.getValueId() != null ? request.getValueId() : UUID.randomUUID());
        valueEntity.setTenantId(tenantId);
        valueEntity.setKpiId(id);
        valueEntity.setPeriodStart(request.getPeriodStart());
        valueEntity.setPeriodEnd(request.getPeriodEnd());
        valueEntity.setActualValue(request.getValue());
        valueEntity.setComment(request.getNotes());
        valueEntity.setCreatedAt(java.time.Instant.now());
        valueEntity.setUpdatedAt(java.time.Instant.now());
        kpiValueRepository.save(valueEntity);
        
        return mapToDTO(entity, true);
    }

    @Override
    @Transactional
    public KpiDTO updateFormula(UUID id, KpiFormulaUpdateRequest request) {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessRuleException("ERR_TENANT_CONTEXT_MISSING", "Tenant context is required");
        }
        
        KpiEntity entity = kpiRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("KPI not found"));

        KPI kpi = mapToDomain(entity);
        kpi.updateFormula(request.getFormula());
        
        entity.setFormula(kpi.getFormula());
        entity = kpiRepository.save(entity);
        
        return mapToDTO(entity, true);
    }

    @Override
    @Transactional
    public KpiDTO calculateKpi(UUID id, KpiCalculateRequest request) {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessRuleException("ERR_TENANT_CONTEXT_MISSING", "Tenant context is required");
        }
        
        KpiEntity entity = kpiRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("KPI not found"));

        KPI kpi = mapToDomain(entity);
        
        if (kpi.getStatus() != KpiStatus.DRAFT && kpi.getStatus() != KpiStatus.ACTIVE) {
            throw new BusinessRuleException("ERR_KPI_INVALID_STATE", "Calculations can only run for DRAFT or ACTIVE KPIs");
        }

        double result = evaluatorService.evaluate(kpi.getFormula(), request.getVariables());
        kpi.updateProgress(BigDecimal.valueOf(result));
        
        entity.setCurrentProgress(kpi.getCurrentProgress());
        entity = kpiRepository.save(entity);
        
        return mapToDTO(entity, true);
    }

    @Override
    @Transactional
    public KpiDTO processApproval(UUID id, KpiApprovalRequest request, UUID currentUserId) {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessRuleException("ERR_TENANT_CONTEXT_MISSING", "Tenant context is required");
        }

        KpiEntity entity = kpiRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("KPI not found"));

        KPI kpi = mapToDomain(entity);

        if (request.getAction() == ApprovalAction.SUBMIT) {
            if (!kpi.getOwnerId().equals(currentUserId)) {
                throw new BusinessRuleException("ERR_KPI_UNAUTHORIZED", "Only the owner can submit the KPI");
            }
            kpi.submitForApproval();
        } else if (request.getAction() == ApprovalAction.APPROVE) {
            if (kpi.getStatus() == KpiStatus.PENDING_MANAGER) {
                if (!organizationClientPort.isDirectManager(currentUserId, kpi.getOwnerId())) {
                    throw new BusinessRuleException("ERR_KPI_UNAUTHORIZED", "Only the direct manager can approve at this stage");
                }
                kpi.approveByManager();
            } else if (kpi.getStatus() == KpiStatus.PENDING_DIRECTOR) {
                if (!organizationClientPort.isDirector(currentUserId, kpi.getOwnerId())) {
                    throw new BusinessRuleException("ERR_KPI_UNAUTHORIZED", "Only the director can approve at this stage");
                }
                kpi.approveByDirector();
            } else {
                throw new BusinessRuleException("ERR_KPI_INVALID_STATE", "KPI is not in a pending state");
            }
        } else if (request.getAction() == ApprovalAction.REJECT) {
            // Revert back to DRAFT for simplification
            kpi = new KPI(kpi.getId(), kpi.getTenantId(), kpi.getTemplateId(), kpi.getOwnerId(), 
                          kpi.getName(), kpi.getFrequency(), KpiStatus.DRAFT, kpi.getTarget(), 
                          kpi.getCurrentProgress(), kpi.getFormula(), kpi.getEvaluationScore(), kpi.getManagerComments(), kpi.getCycleId());
        } else {
            throw new IllegalArgumentException("Unknown action: " + request.getAction());
        }

        entity.setStatus(kpi.getStatus());
        entity = kpiRepository.save(entity);

        return mapToDTO(entity, true);
    }

    @Override
    @Transactional
    public KpiDTO completeKpi(UUID id, com.enterprise.kpi.application.dto.KpiCompleteRequest request) {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessRuleException("ERR_TENANT_CONTEXT_MISSING", "Tenant context is required");
        }

        KpiEntity entity = kpiRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("KPI not found"));

        KPI kpi = mapToDomain(entity);
        kpi.complete(request.getFinalScore(), request.getManagerComments());

        entity.setStatus(kpi.getStatus());
        entity.setEvaluationScore(kpi.getEvaluationScore());
        entity.setManagerComments(kpi.getManagerComments());
        entity = kpiRepository.save(entity);

        return mapToDTO(entity, true);
    }

    @Override
    @Transactional
    public void deleteKpi(UUID id) {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessRuleException("ERR_TENANT_CONTEXT_MISSING", "Tenant context is required");
        }
        
        KpiEntity entity = kpiRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("KPI not found"));
                
        if (entity.getStatus() != KpiStatus.DRAFT) {
            throw new BusinessRuleException("ERR_KPI_INVALID_STATE", "Only DRAFT KPIs can be deleted");
        }
        
        kpiRepository.delete(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<KpiDTO> getKpis(
            org.springframework.data.domain.Pageable pageable, 
            String statusStr, 
            UUID ownerId) {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessRuleException("ERR_TENANT_CONTEXT_MISSING", "Tenant context is required");
        }

        KpiStatus status = null;
        if (statusStr != null && !statusStr.trim().isEmpty()) {
            try {
                status = KpiStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Ignore invalid status
            }
        }

        org.springframework.data.domain.Page<KpiEntity> entitiesPage;
        if (status != null && ownerId != null) {
            entitiesPage = kpiRepository.findByTenantIdAndStatusAndOwnerId(tenantId, status, ownerId, pageable);
        } else if (status != null) {
            entitiesPage = kpiRepository.findByTenantIdAndStatus(tenantId, status, pageable);
        } else if (ownerId != null) {
            entitiesPage = kpiRepository.findByTenantIdAndOwnerId(tenantId, ownerId, pageable);
        } else {
            entitiesPage = kpiRepository.findByTenantId(tenantId, pageable);
        }

        return entitiesPage.map(e -> mapToDTO(e, false));
    }

    private KPI mapToDomain(KpiEntity entity) {
        return new KPI(
            entity.getId(),
            entity.getTenantId(),
            entity.getTemplateId(),
            entity.getOwnerId(),
            entity.getName(),
            entity.getFrequency(),
            entity.getStatus(),
            entity.getTarget(),
            entity.getCurrentProgress(),
            entity.getFormula(),
            entity.getEvaluationScore(),
            entity.getManagerComments(),
            entity.getCycleId()
        );
    }

    private KpiDTO mapToDTO(KpiEntity entity, boolean loadValues) {
        List<KpiValueDTO> valueDTOs = null;
        if (loadValues) {
            valueDTOs = kpiValueRepository.findByKpiIdAndTenantIdOrderByPeriodStartDesc(entity.getId(), entity.getTenantId())
                .stream()
                .map(val -> KpiValueDTO.builder()
                        .id(val.getId())
                        .periodStart(val.getPeriodStart())
                        .periodEnd(val.getPeriodEnd())
                        .actualValue(val.getActualValue())
                        .progressPercent(val.getProgressPercent())
                        .comment(val.getComment())
                        .evidence(val.getEvidence())
                        .attachments(kpiAttachmentService.listAttachments(val.getId()))
                        .build())
                .collect(Collectors.toList());
        }

        return KpiDTO.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .templateId(entity.getTemplateId())
                .ownerId(entity.getOwnerId())
                .name(entity.getName())
                .frequency(entity.getFrequency())
                .status(entity.getStatus())
                .target(entity.getTarget())
                .currentProgress(entity.getCurrentProgress())
                .formula(entity.getFormula())
                .evaluationScore(entity.getEvaluationScore())
                .managerComments(entity.getManagerComments())
                .cycleId(entity.getCycleId())
                .updatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt().atOffset(ZoneOffset.UTC) : null)
                .values(valueDTOs)
                .build();
    }
}
