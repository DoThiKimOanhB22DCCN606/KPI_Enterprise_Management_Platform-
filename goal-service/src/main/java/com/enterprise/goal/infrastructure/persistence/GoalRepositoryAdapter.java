package com.enterprise.goal.infrastructure.persistence;

import com.enterprise.goal.domain.model.Goal;
import com.enterprise.goal.domain.repository.GoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Component
@RequiredArgsConstructor
public class GoalRepositoryAdapter implements GoalRepository {

    private final GoalJpaRepository repository;

    @Override
    public List<Goal> findAllByParentGoalIdAndTenantId(UUID parentGoalId, UUID tenantId) {
        return repository.findAllByParentGoalIdAndTenantId(parentGoalId, tenantId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Goal> findByKpiIdAndTenantId(UUID kpiId, UUID tenantId) {
        return repository.findByKpiIdAndTenantId(kpiId, tenantId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Goal> findByIdAndTenantId(UUID id, UUID tenantId) {
        return repository.findByIdAndTenantId(id, tenantId).map(this::toDomain);
    }

    @Override
    public Page<Goal> findAllByTenantId(UUID tenantId, Pageable pageable) {
        return repository.findAllByTenantId(tenantId, pageable).map(this::toDomain);
    }

    @Override
    public Page<Goal> findFiltered(UUID tenantId, UUID ownerId, String ownerType, String status, Pageable pageable) {
        return repository.findFiltered(tenantId, ownerId, ownerType, status, pageable).map(this::toDomain);
    }

    @Override
    public Goal save(Goal goal) {
        GoalEntity entity = toEntity(goal);
        return toDomain(repository.save(entity));
    }

    @Override
    public void delete(Goal goal) {
        repository.deleteById(goal.getId());
    }

    private GoalEntity toEntity(Goal domain) {
        GoalEntity entity = new GoalEntity();
        entity.setId(domain.getId() == null ? UUID.randomUUID() : domain.getId());
        entity.setTenantId(domain.getTenantId());
        entity.setParentGoalId(domain.getParentGoalId());
        entity.setKpiId(domain.getKpiId());
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setTargetValue(domain.getTargetValue());
        entity.setCurrentValue(domain.getCurrentValue());
        entity.setOverallProgress(domain.getOverallProgress());
        entity.setWeight(domain.getWeight());
        entity.setOwnerId(domain.getOwnerId());
        entity.setOwnerType(domain.getOwnerType());
        entity.setStatus(domain.getStatus());
        return entity;
    }

    private Goal toDomain(GoalEntity entity) {
        return Goal.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .parentGoalId(entity.getParentGoalId())
                .kpiId(entity.getKpiId())
                .name(entity.getName())
                .description(entity.getDescription())
                .targetValue(entity.getTargetValue())
                .currentValue(entity.getCurrentValue())
                .overallProgress(entity.getOverallProgress())
                .weight(entity.getWeight())
                .ownerId(entity.getOwnerId())
                .ownerType(entity.getOwnerType())
                .status(entity.getStatus())
                .build();
    }
}
