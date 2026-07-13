package com.enterprise.kpi.application.service;

import com.enterprise.kpi.application.dto.CreateEvaluationCycleRequest;
import com.enterprise.kpi.application.dto.EvaluationCycleDTO;
import com.enterprise.kpi.application.dto.UpdateEvaluationCycleRequest;
import com.enterprise.kpi.domain.model.CycleStatus;
import com.enterprise.kpi.domain.model.EvaluationCycle;
import com.enterprise.kpi.infrastructure.messaging.KpiEventPublisher;
import com.enterprise.kpi.infrastructure.messaging.event.AuditEventDTO;
import com.enterprise.kpi.infrastructure.persistence.EvaluationCycleEntity;
import com.enterprise.kpi.infrastructure.persistence.EvaluationCycleRepository;
import com.enterprise.kpi.infrastructure.persistence.KpiEntity;
import com.enterprise.kpi.infrastructure.persistence.KpiRepository;
import com.enterprise.kpi.domain.model.KPI;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EvaluationCycleService {

    private final EvaluationCycleRepository repository;
    private final KpiRepository kpiRepository;
    private final KpiEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public List<EvaluationCycleDTO> listCycles(UUID tenantId) {
        return repository.findAllByTenantId(tenantId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EvaluationCycleDTO getCycle(UUID tenantId, UUID id) {
        return mapToDTO(getEntity(tenantId, id));
    }

    @Transactional
    public EvaluationCycleDTO createCycle(UUID tenantId, CreateEvaluationCycleRequest request) {
        EvaluationCycle cycle = EvaluationCycle.create(
                UUID.randomUUID(),
                tenantId,
                request.getName(),
                request.getType(),
                request.getPeriodStart(),
                request.getPeriodEnd()
        );

        EvaluationCycleEntity entity = mapToEntity(cycle);
        return mapToDTO(repository.save(entity));
    }

    @Transactional
    public EvaluationCycleDTO updateCycle(UUID tenantId, UUID id, UpdateEvaluationCycleRequest request) {
        EvaluationCycleEntity entity = getEntity(tenantId, id);
        EvaluationCycle cycle = mapToDomain(entity);

        if (cycle.getStatus() == CycleStatus.OPEN) {
            if (request.getReason() == null || request.getReason().trim().isEmpty()) {
                throw new IllegalArgumentException("ERR_MANDATORY_REASON: Reason is mandatory when updating an ACTIVE cycle");
            }
            
            // Generate Audit Log
            Map<String, Object> changes = new HashMap<>();
            Map<String, Object> before = new HashMap<>();
            before.put("name", cycle.getName());
            before.put("periodStart", cycle.getPeriodStart().toString());
            before.put("periodEnd", cycle.getPeriodEnd().toString());
            
            Map<String, Object> after = new HashMap<>();
            after.put("name", request.getName());
            after.put("periodStart", request.getPeriodStart().toString());
            after.put("periodEnd", request.getPeriodEnd().toString());
            
            changes.put("before", before);
            changes.put("after", after);
            changes.put("reason", request.getReason());
            
            AuditEventDTO auditEvent = AuditEventDTO.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("CYCLE_UPDATED")
                    .actorId("system") // This should be extracted from SecurityContext in reality
                    .resourceId(id.toString())
                    .changes(changes)
                    .timestamp(System.currentTimeMillis())
                    .build();
                    
            eventPublisher.publishAuditEvent(auditEvent);
        }

        cycle.updateDetails(request.getName(), request.getPeriodStart(), request.getPeriodEnd());
        updateEntityFromDomain(entity, cycle);
        
        return mapToDTO(repository.save(entity));
    }

    @Transactional
    public EvaluationCycleDTO activateCycle(UUID tenantId, UUID id) {
        EvaluationCycleEntity entity = getEntity(tenantId, id);
        EvaluationCycle cycle = mapToDomain(entity);
        
        cycle.open();
        
        updateEntityFromDomain(entity, cycle);
        return mapToDTO(repository.save(entity));
    }

    @Transactional
    public EvaluationCycleDTO closeCycle(UUID tenantId, UUID id) {
        EvaluationCycleEntity entity = getEntity(tenantId, id);
        EvaluationCycle cycle = mapToDomain(entity);
        
        if (cycle.getStatus() == CycleStatus.OPEN) {
            cycle.lock();
            cycle.close();
        } else {
            cycle.close();
        }
        
        List<KpiEntity> kpiEntities = kpiRepository.findByTenantIdAndCycleId(tenantId, id);
        
        for (KpiEntity kpiEntity : kpiEntities) {
            KPI kpi = mapKpiToDomain(kpiEntity);
            String oldStatus = kpi.getStatus().toString();
            kpi.forceClose("Parent Evaluation Cycle Closed");
            updateKpiEntityFromDomain(kpiEntity, kpi);
            
            Map<String, Object> changes = new HashMap<>();
            changes.put("reason", "Parent Evaluation Cycle Closed");
            changes.put("oldStatus", oldStatus);
            changes.put("newStatus", "CLOSED");
            
            AuditEventDTO auditEvent = AuditEventDTO.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("KPI_CLOSED")
                    .actorId("system")
                    .resourceId(kpi.getId().toString())
                    .changes(changes)
                    .timestamp(System.currentTimeMillis())
                    .build();
            eventPublisher.publishAuditEvent(auditEvent);
        }
        kpiRepository.saveAll(kpiEntities);
        
        updateEntityFromDomain(entity, cycle);
        return mapToDTO(repository.save(entity));
    }

    private KPI mapKpiToDomain(KpiEntity entity) {
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

    private void updateKpiEntityFromDomain(KpiEntity entity, KPI domain) {
        entity.setStatus(domain.getStatus());
        entity.setCurrentProgress(domain.getCurrentProgress());
        entity.setEvaluationScore(domain.getEvaluationScore());
        entity.setManagerComments(domain.getManagerComments());
    }

    private EvaluationCycleEntity getEntity(UUID tenantId, UUID id) {
        return repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Cycle not found"));
    }

    private EvaluationCycle mapToDomain(EvaluationCycleEntity entity) {
        return new EvaluationCycle(
                entity.getId(),
                entity.getTenantId(),
                entity.getName(),
                entity.getType(),
                entity.getPeriodStart(),
                entity.getPeriodEnd(),
                entity.getStatus(),
                entity.getKpiCount()
        );
    }

    private EvaluationCycleEntity mapToEntity(EvaluationCycle domain) {
        EvaluationCycleEntity entity = new EvaluationCycleEntity();
        entity.setId(domain.getId());
        entity.setTenantId(domain.getTenantId());
        entity.setName(domain.getName());
        entity.setType(domain.getType());
        entity.setPeriodStart(domain.getPeriodStart());
        entity.setPeriodEnd(domain.getPeriodEnd());
        entity.setStatus(domain.getStatus());
        entity.setKpiCount(domain.getKpiCount());
        return entity;
    }

    private void updateEntityFromDomain(EvaluationCycleEntity entity, EvaluationCycle domain) {
        entity.setName(domain.getName());
        entity.setPeriodStart(domain.getPeriodStart());
        entity.setPeriodEnd(domain.getPeriodEnd());
        entity.setStatus(domain.getStatus());
    }

    private EvaluationCycleDTO mapToDTO(EvaluationCycleEntity entity) {
        return EvaluationCycleDTO.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .name(entity.getName())
                .type(entity.getType())
                .periodStart(entity.getPeriodStart())
                .periodEnd(entity.getPeriodEnd())
                .status(entity.getStatus())
                .kpiCount(entity.getKpiCount())
                .build();
    }
}
