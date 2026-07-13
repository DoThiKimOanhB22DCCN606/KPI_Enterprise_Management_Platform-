package com.enterprise.kpi.application.service;

import com.enterprise.kpi.application.dto.*;
import com.enterprise.kpi.domain.model.KPI;
import com.enterprise.kpi.domain.model.KpiTemplate;
import com.enterprise.kpi.infrastructure.config.TenantContext;
import com.enterprise.kpi.infrastructure.persistence.KpiEntity;
import com.enterprise.kpi.infrastructure.persistence.KpiRepository;
import com.enterprise.kpi.infrastructure.persistence.KpiTemplateEntity;
import com.enterprise.kpi.infrastructure.persistence.KpiTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KpiTemplateService {

    private final KpiTemplateRepository templateRepository;
    private final KpiRepository kpiRepository;

    @Transactional(readOnly = true)
    public List<KpiTemplateDTO> listTemplates(String category) {
        UUID tenantId = getTenantId();
        List<KpiTemplateEntity> entities = (category != null && !category.isBlank())
                ? templateRepository.findAllByTenantIdAndCategory(tenantId, category)
                : templateRepository.findAllByTenantId(tenantId);
        return entities.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional
    public KpiTemplateDTO createTemplate(KpiTemplateCreateRequest request) {
        KpiTemplate template = KpiTemplate.builder()
                .id(UUID.randomUUID())
                .tenantId(getTenantId())
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .defaultFrequency(request.getDefaultFrequency())
                .defaultTarget(request.getDefaultTarget())
                .defaultFormula(request.getDefaultFormula())
                .build();
        return mapToDTO(templateRepository.save(mapToEntity(template)));
    }

    @Transactional(readOnly = true)
    public KpiTemplateDTO getTemplate(UUID id) {
        return mapToDTO(getTemplateEntity(id));
    }

    @Transactional
    public KpiTemplateDTO updateTemplate(UUID id, KpiTemplateUpdateRequest request) {
        KpiTemplateEntity entity = getTemplateEntity(id);
        KpiTemplate template = mapToDomain(entity);
        if (request.getName() != null) template.setName(request.getName());
        if (request.getDescription() != null) template.setDescription(request.getDescription());
        if (request.getCategory() != null) template.setCategory(request.getCategory());
        if (request.getDefaultFrequency() != null) template.setDefaultFrequency(request.getDefaultFrequency());
        if (request.getDefaultTarget() != null) template.setDefaultTarget(request.getDefaultTarget());
        if (request.getDefaultFormula() != null) template.setDefaultFormula(request.getDefaultFormula());
        return mapToDTO(templateRepository.save(mapToEntity(template)));
    }

    @Transactional
    public void deleteTemplate(UUID id) {
        KpiTemplateEntity entity = getTemplateEntity(id);
        templateRepository.delete(entity);
    }

    @Transactional
    public KpiTemplateDTO cloneTemplate(UUID id) {
        KpiTemplate template = mapToDomain(getTemplateEntity(id));
        KpiTemplate clonedTemplate = template.cloneTemplate();
        return mapToDTO(templateRepository.save(mapToEntity(clonedTemplate)));
    }

    @Transactional
    public KpiDTO applyTemplate(UUID templateId, UUID ownerId) {
        KpiTemplate template = mapToDomain(getTemplateEntity(templateId));

        KPI kpi = KPI.create(
                UUID.randomUUID(),
                getTenantId(),
                template.getId(),
                ownerId,
                template.getName(),
                template.getDefaultFrequency(),
                template.getDefaultTarget(),
                template.getDefaultFormula(),
                null
        );

        KpiEntity entity = new KpiEntity();
        entity.setId(kpi.getId());
        entity.setTenantId(kpi.getTenantId());
        entity.setTemplateId(kpi.getTemplateId());
        entity.setOwnerId(kpi.getOwnerId());
        entity.setName(kpi.getName());
        entity.setFrequency(kpi.getFrequency());
        entity.setStatus(kpi.getStatus());
        entity.setTarget(kpi.getTarget());
        entity.setCurrentProgress(kpi.getCurrentProgress());
        entity.setFormula(kpi.getFormula());

        entity = kpiRepository.save(entity);

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
                .build();
    }

    private UUID getTenantId() {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) tenantId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        return tenantId;
    }

    private KpiTemplateEntity getTemplateEntity(UUID id) {
        return templateRepository.findByIdAndTenantId(id, getTenantId())
                .orElseThrow(() -> new RuntimeException("KPI Template not found"));
    }

    private KpiTemplateEntity mapToEntity(KpiTemplate domain) {
        KpiTemplateEntity entity = new KpiTemplateEntity();
        entity.setId(domain.getId());
        entity.setTenantId(domain.getTenantId());
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setCategory(domain.getCategory());
        entity.setDefaultFrequency(domain.getDefaultFrequency());
        entity.setDefaultTarget(domain.getDefaultTarget());
        entity.setDefaultFormula(domain.getDefaultFormula());
        return entity;
    }

    private KpiTemplate mapToDomain(KpiTemplateEntity entity) {
        return KpiTemplate.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .name(entity.getName())
                .description(entity.getDescription())
                .category(entity.getCategory())
                .defaultFrequency(entity.getDefaultFrequency())
                .defaultTarget(entity.getDefaultTarget())
                .defaultFormula(entity.getDefaultFormula())
                .build();
    }

    private KpiTemplateDTO mapToDTO(KpiTemplateEntity entity) {
        return KpiTemplateDTO.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .name(entity.getName())
                .description(entity.getDescription())
                .category(entity.getCategory())
                .defaultFrequency(entity.getDefaultFrequency())
                .defaultTarget(entity.getDefaultTarget())
                .defaultFormula(entity.getDefaultFormula())
                .build();
    }
}
