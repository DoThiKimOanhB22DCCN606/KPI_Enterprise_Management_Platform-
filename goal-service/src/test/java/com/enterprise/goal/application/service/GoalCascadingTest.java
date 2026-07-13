package com.enterprise.goal.application.service;

import com.enterprise.goal.domain.model.Goal;
import com.enterprise.goal.domain.repository.GoalRepository;
import com.enterprise.goal.infrastructure.messaging.GoalEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GoalCascadingTest {

    @Mock
    private GoalRepository goalRepo;

    @Mock
    private GoalEventPublisher eventPublisher;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private GoalRecalculationService recalculationService;

    private UUID tenantId;
    private UUID parentGoalId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        parentGoalId = UUID.randomUUID();
    }

    @Test
    void testCalculateAndPublishParentProgress_CalculatesWeightedAverage() {
        // Arrange
        Goal child1 = Goal.builder()
                .id(UUID.randomUUID())
                .parentGoalId(parentGoalId)
                .overallProgress(80.0)
                .weight(2.0)
                .build();

        Goal child2 = Goal.builder()
                .id(UUID.randomUUID())
                .parentGoalId(parentGoalId)
                .overallProgress(50.0)
                .weight(3.0)
                .build();

        when(goalRepo.findAllByParentGoalIdAndTenantId(parentGoalId, tenantId))
                .thenReturn(List.of(child1, child2));

        // Act
        // We pass one of the children to trigger the parent recalculation
        recalculationService.calculateAndPublishParentProgress(child1, tenantId);

        // Assert
        // Expected average = (80 * 2 + 50 * 3) / (2 + 3) = (160 + 150) / 5 = 310 / 5 = 62.0
        ArgumentCaptor<Double> progressCaptor = ArgumentCaptor.forClass(Double.class);
        
        verify(eventPublisher).publishGoalRecalculated(
                eq(parentGoalId),
                eq(tenantId),
                progressCaptor.capture()
        );

        assertEquals(62.0, progressCaptor.getValue(), 0.01);
    }

    @Test
    void testRecalculate_CalculatesOverallProgress_AndCallsParent() {
        Goal goal = Goal.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .parentGoalId(parentGoalId)
                .kpiId(UUID.randomUUID()) // has KPI
                .targetValue(200.0)
                .overallProgress(0.0) // initial
                .build();

        when(goalRepo.findByIdAndTenantId(goal.getId(), tenantId)).thenReturn(java.util.Optional.of(goal));
        when(jdbcTemplate.queryForObject(anyString(), eq(Double.class), any(), any())).thenReturn(0.0);
        
        // Mocking siblings for parent calculation
        when(goalRepo.findAllByParentGoalIdAndTenantId(parentGoalId, tenantId)).thenReturn(List.of(goal));

        recalculationService.recalculate(goal.getId(), tenantId);

        // Since fetchKpiProgress returns 0.0 mock, overall progress should be 0.0
        // Current value should be 200 * 0.0 / 100 = 0.0
        verify(goalRepo).save(goal);
        assertEquals(0.0, goal.getOverallProgress(), 0.01);
        assertEquals(0.0, goal.getCurrentValue(), 0.01);

        // Should call parent publish
        verify(eventPublisher).publishGoalRecalculated(eq(parentGoalId), eq(tenantId), anyDouble());
    }

    @Test
    void testRecalculate_GoalNotFound_ThrowsException() {
        UUID randomId = UUID.randomUUID();
        when(goalRepo.findByIdAndTenantId(randomId, tenantId)).thenReturn(java.util.Optional.empty());

        RuntimeException ex = org.junit.jupiter.api.Assertions.assertThrows(
            RuntimeException.class, () -> recalculationService.recalculate(randomId, tenantId)
        );
        assertEquals("Goal not found", ex.getMessage());
    }
}
