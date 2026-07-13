package com.enterprise.goal.application.service;

import com.enterprise.goal.domain.model.Goal;
import com.enterprise.goal.domain.repository.GoalRepository;
import com.enterprise.goal.infrastructure.messaging.GoalEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.EmptyResultDataAccessException;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoalRecalculationService {

    private final GoalRepository goalRepo;
    private final GoalEventPublisher eventPublisher;
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void recalculate(UUID goalId, UUID tenantId) {
        Goal goal = goalRepo.findByIdAndTenantId(goalId, tenantId)
                .orElseThrow(() -> new RuntimeException("Goal not found"));
        
        if (goal.getKpiId() != null) {
            double kpiProgress = fetchKpiProgress(goal.getKpiId(), tenantId);
            goal.setOverallProgress(kpiProgress);
        }
        
        goal.setCurrentValue(goal.getTargetValue() * goal.getOverallProgress() / 100.0);
        goalRepo.save(goal);
        
        if (goal.getParentGoalId() != null) {
            calculateAndPublishParentProgress(goal, tenantId);
        }
    }

    public void calculateAndPublishParentProgress(Goal goal, UUID tenantId) {
        if (goal.getParentGoalId() == null) return;
        
        List<Goal> siblings = goalRepo.findAllByParentGoalIdAndTenantId(
            goal.getParentGoalId(), tenantId);
        
        double totalWeight = siblings.stream().mapToDouble(Goal::getWeight).sum();
        double weightedSum = siblings.stream()
            .mapToDouble(g -> g.getOverallProgress() * g.getWeight())
            .sum();
        double parentProgress = totalWeight > 0 ? (weightedSum / totalWeight) : 0.0;
        
        eventPublisher.publishGoalRecalculated(
            goal.getParentGoalId(), tenantId, parentProgress);
    }

    private double fetchKpiProgress(UUID kpiId, UUID tenantId) {
        try {
            Double progress = jdbcTemplate.queryForObject(
                "SELECT current_progress FROM kpis WHERE id = ? AND tenant_id = ? AND deleted_at IS NULL", 
                Double.class, 
                kpiId, 
                tenantId
            );
            return progress != null ? progress : 0.0;
        } catch (EmptyResultDataAccessException e) {
            log.warn("KPI not found or deleted when fetching progress: {}", kpiId);
            return 0.0;
        }
    }

    @Transactional
    public void recalculateCascadingGoals(com.enterprise.goal.infrastructure.messaging.event.KpiProgressUpdatedEvent event) {
        if (event.getKpiId() == null || event.getTenantId() == null || event.getNewProgress() == null) return;
        
        List<Goal> goals = goalRepo.findByKpiIdAndTenantId(event.getKpiId(), event.getTenantId());
        for (Goal goal : goals) {
            goal.setOverallProgress(event.getNewProgress().doubleValue());
            goal.setCurrentValue(goal.getTargetValue() * goal.getOverallProgress() / 100.0);
            goalRepo.save(goal);
            
            recalculateParent(goal.getParentGoalId(), event.getTenantId());
        }
    }

    private void recalculateParent(UUID parentId, UUID tenantId) {
        if (parentId == null) return;
        
        Goal parent = goalRepo.findByIdAndTenantId(parentId, tenantId).orElse(null);
        if (parent == null) return;
        
        List<Goal> siblings = goalRepo.findAllByParentGoalIdAndTenantId(parentId, tenantId);
        
        double totalWeight = siblings.stream().mapToDouble(Goal::getWeight).sum();
        double weightedSum = siblings.stream()
            .mapToDouble(g -> g.getOverallProgress() * g.getWeight())
            .sum();
        
        double parentProgress = totalWeight > 0 ? (weightedSum / totalWeight) : 0.0;
        
        parent.setOverallProgress(parentProgress);
        parent.setCurrentValue(parent.getTargetValue() * parentProgress / 100.0);
        goalRepo.save(parent);
        
        eventPublisher.publishGoalRecalculated(parentId, tenantId, parentProgress);
        
        recalculateParent(parent.getParentGoalId(), tenantId);
    }
}
