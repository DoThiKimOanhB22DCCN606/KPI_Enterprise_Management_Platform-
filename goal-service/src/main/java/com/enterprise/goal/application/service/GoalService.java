package com.enterprise.goal.application.service;

import com.enterprise.goal.domain.model.Goal;
import com.enterprise.goal.domain.repository.GoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.enterprise.goal.application.dto.GoalNodeDTO;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoalService {

    private final GoalRepository goalRepo;
    
    public Goal createGoal(Goal goal, UUID tenantId) {
        validateGoalTarget(goal.getParentGoalId(), goal.getTargetValue(), tenantId);
        goal.setTenantId(tenantId);
        return goalRepo.save(goal);
    }
    
    public Goal updateGoal(UUID id, Goal goal, UUID tenantId) {
        validateGoalTarget(goal.getParentGoalId(), goal.getTargetValue(), tenantId);
        goal.setId(id);
        goal.setTenantId(tenantId);
        return goalRepo.save(goal);
    }
    
    public Goal updateGoalStatus(UUID id, String newStatus, UUID tenantId) {
        Goal goal = getGoal(id, tenantId);
        String currentStatus = goal.getStatus() == null ? "DRAFT" : goal.getStatus().toUpperCase();
        newStatus = newStatus.toUpperCase();
        
        if ("ACTIVE".equals(newStatus)) {
            if (!"DRAFT".equals(currentStatus)) {
                throw new RuntimeException("Can only activate DRAFT goals");
            }
        } else if ("COMPLETED".equals(newStatus) || "CANCELLED".equals(newStatus)) {
            if (!"ACTIVE".equals(currentStatus)) {
                throw new RuntimeException("Can only complete or cancel ACTIVE goals");
            }
        } else if (!"DRAFT".equals(newStatus)) {
            throw new RuntimeException("Invalid status transition: " + currentStatus + " -> " + newStatus);
        }
        
        goal.setStatus(newStatus);
        return goalRepo.save(goal);
    }
    
    public Page<Goal> listGoals(UUID tenantId, UUID ownerId, String ownerType, String status, Pageable pageable) {
        if (ownerId == null && ownerType == null && status == null) {
            return goalRepo.findAllByTenantId(tenantId, pageable);
        }
        return goalRepo.findFiltered(tenantId, ownerId, ownerType, status, pageable);
    }

    public Goal getGoal(UUID id, UUID tenantId) {
        return goalRepo.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Goal not found"));
    }

    public void deleteGoal(UUID id, UUID tenantId) {
        Goal goal = getGoal(id, tenantId);
        if (!"DRAFT".equalsIgnoreCase(goal.getStatus())) {
            throw new RuntimeException("Only DRAFT goals can be deleted");
        }
        goalRepo.delete(goal);
    }

    public GoalNodeDTO getGoalTree(UUID id, UUID tenantId) {
        Goal rootGoal = getGoal(id, tenantId);
        return buildTree(rootGoal, tenantId);
    }

    private GoalNodeDTO buildTree(Goal goal, UUID tenantId) {
        List<Goal> children = goalRepo.findAllByParentGoalIdAndTenantId(goal.getId(), tenantId);
        List<GoalNodeDTO> childrenNodes = children.stream()
                .map(child -> buildTree(child, tenantId))
                .collect(Collectors.toList());
                
        return GoalNodeDTO.builder()
                .goal(goal)
                .children(childrenNodes)
                .build();
    }
    
    private void validateGoalTarget(UUID parentGoalId, double childTarget, UUID tenantId) {
        if (parentGoalId == null) return; // root goal, no constraint
        Goal parent = goalRepo.findByIdAndTenantId(parentGoalId, tenantId)
            .orElseThrow(() -> new RuntimeException("Parent Goal Not Found"));
        if (childTarget > parent.getTargetValue()) {
            throw new RuntimeException(
                String.format("ERR_GOAL_TARGET_EXCEEDS_PARENT: Child goal target (%.2f) cannot exceed parent goal target (%.2f)",
                    childTarget, parent.getTargetValue()));
        }
    }
}
