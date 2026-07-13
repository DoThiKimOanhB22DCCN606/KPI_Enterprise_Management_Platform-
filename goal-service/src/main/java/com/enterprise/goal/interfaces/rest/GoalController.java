package com.enterprise.goal.interfaces.rest;

import com.enterprise.goal.application.service.GoalService;
import com.enterprise.goal.domain.model.Goal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import com.enterprise.goal.infrastructure.config.TenantContext;

@RestController
@RequestMapping("/v1/goals")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;
    
    @PostMapping
    public ResponseEntity<Goal> createGoal(@RequestBody Goal goal) {
        UUID tenantId = requireTenantId();
        return ResponseEntity.status(HttpStatus.CREATED).body(goalService.createGoal(goal, tenantId));
    }

    @GetMapping
    public ResponseEntity<org.springframework.data.domain.Page<Goal>> listGoals(
            @RequestParam(required = false) UUID ownerId,
            @RequestParam(required = false) String ownerType,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        UUID tenantId = requireTenantId();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        return ResponseEntity.ok(goalService.listGoals(tenantId, ownerId, ownerType, status, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Goal> getGoal(@PathVariable UUID id) {
        UUID tenantId = requireTenantId();
        return ResponseEntity.ok(goalService.getGoal(id, tenantId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Goal> updateGoal(
            @PathVariable UUID id,
            @RequestBody Goal goal) {
        UUID tenantId = requireTenantId();
        return ResponseEntity.ok(goalService.updateGoal(id, goal, tenantId));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Goal> updateGoalStatus(
            @PathVariable UUID id,
            @RequestParam String status) {
        UUID tenantId = requireTenantId();
        return ResponseEntity.ok(goalService.updateGoalStatus(id, status, tenantId));
    }

    @GetMapping("/{id}/tree")
    public ResponseEntity<com.enterprise.goal.application.dto.GoalNodeDTO> getGoalTree(
            @PathVariable UUID id) {
        UUID tenantId = requireTenantId();
        return ResponseEntity.ok(goalService.getGoalTree(id, tenantId));
    }

    @PostMapping("/{id}/link-kpi")
    public ResponseEntity<Goal> linkKpiToGoal(
            @PathVariable UUID id, 
            @RequestParam UUID kpiId) {
        UUID tenantId = requireTenantId();
        Goal goal = Goal.builder().id(id).kpiId(kpiId).build();
        return ResponseEntity.ok(goalService.updateGoal(id, goal, tenantId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGoal(@PathVariable UUID id) {
        UUID tenantId = requireTenantId();
        goalService.deleteGoal(id, tenantId);
        return ResponseEntity.noContent().build();
    }
    
    private UUID requireTenantId() {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "Tenant context missing");
        }
        return tenantId;
    }
}
