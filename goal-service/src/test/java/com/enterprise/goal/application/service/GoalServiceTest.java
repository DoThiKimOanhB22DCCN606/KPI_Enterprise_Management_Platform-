package com.enterprise.goal.application.service;

import com.enterprise.goal.application.dto.GoalNodeDTO;
import com.enterprise.goal.domain.model.Goal;
import com.enterprise.goal.domain.repository.GoalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GoalServiceTest {

    @Mock
    private GoalRepository goalRepo;

    @InjectMocks
    private GoalService goalService;

    private UUID tenantId;
    private UUID goalId;
    private UUID parentGoalId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        goalId = UUID.randomUUID();
        parentGoalId = UUID.randomUUID();
    }

    @Test
    void createGoal_RootGoal_Success() {
        Goal newGoal = Goal.builder().targetValue(100.0).build();

        when(goalRepo.save(any(Goal.class))).thenAnswer(i -> i.getArguments()[0]);

        Goal result = goalService.createGoal(newGoal, tenantId);

        assertNotNull(result);
        assertEquals(tenantId, result.getTenantId());
        verify(goalRepo).save(any(Goal.class));
    }

    @Test
    void createGoal_ChildGoal_ValidTarget_Success() {
        Goal parentGoal = Goal.builder().id(parentGoalId).targetValue(100.0).build();
        Goal childGoal = Goal.builder().parentGoalId(parentGoalId).targetValue(50.0).build();

        when(goalRepo.findByIdAndTenantId(parentGoalId, tenantId)).thenReturn(Optional.of(parentGoal));
        when(goalRepo.save(any(Goal.class))).thenAnswer(i -> i.getArguments()[0]);

        Goal result = goalService.createGoal(childGoal, tenantId);

        assertNotNull(result);
        assertEquals(tenantId, result.getTenantId());
    }

    @Test
    void createGoal_ChildGoal_TargetExceedsParent_ThrowsException() {
        Goal parentGoal = Goal.builder().id(parentGoalId).targetValue(100.0).build();
        Goal childGoal = Goal.builder().parentGoalId(parentGoalId).targetValue(150.0).build();

        when(goalRepo.findByIdAndTenantId(parentGoalId, tenantId)).thenReturn(Optional.of(parentGoal));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> goalService.createGoal(childGoal, tenantId));
        assertTrue(ex.getMessage().contains("ERR_GOAL_TARGET_EXCEEDS_PARENT"));
        verify(goalRepo, never()).save(any(Goal.class));
    }

    @Test
    void updateGoal_Success() {
        Goal goal = Goal.builder().targetValue(80.0).build();

        when(goalRepo.save(any(Goal.class))).thenAnswer(i -> i.getArguments()[0]);

        Goal result = goalService.updateGoal(goalId, goal, tenantId);

        assertEquals(goalId, result.getId());
        assertEquals(tenantId, result.getTenantId());
        verify(goalRepo).save(any(Goal.class));
    }

    @Test
    void listGoals_Success() {
        Goal goal = Goal.builder().id(goalId).build();
        Pageable pageable = PageRequest.of(0, 10);
        when(goalRepo.findAllByTenantId(tenantId, pageable)).thenReturn(new PageImpl<>(List.of(goal)));

        Page<Goal> result = goalService.listGoals(tenantId, null, null, null, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(goalId, result.getContent().get(0).getId());
    }

    @Test
    void getGoal_NotFound_ThrowsException() {
        when(goalRepo.findByIdAndTenantId(goalId, tenantId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> goalService.getGoal(goalId, tenantId));
    }

    @Test
    void getGoalTree_Success() {
        Goal rootGoal = Goal.builder().id(goalId).targetValue(100.0).build();
        Goal childGoal = Goal.builder().id(UUID.randomUUID()).parentGoalId(goalId).targetValue(50.0).build();

        when(goalRepo.findByIdAndTenantId(goalId, tenantId)).thenReturn(Optional.of(rootGoal));
        when(goalRepo.findAllByParentGoalIdAndTenantId(goalId, tenantId)).thenReturn(List.of(childGoal));
        when(goalRepo.findAllByParentGoalIdAndTenantId(childGoal.getId(), tenantId)).thenReturn(List.of());

        GoalNodeDTO tree = goalService.getGoalTree(goalId, tenantId);

        assertNotNull(tree);
        assertEquals(goalId, tree.getGoal().getId());
        assertEquals(1, tree.getChildren().size());
        assertEquals(childGoal.getId(), tree.getChildren().get(0).getGoal().getId());
        assertTrue(tree.getChildren().get(0).getChildren().isEmpty());
    }
}
