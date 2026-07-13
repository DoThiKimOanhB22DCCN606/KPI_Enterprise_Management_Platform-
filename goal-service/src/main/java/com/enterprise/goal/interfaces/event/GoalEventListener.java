package com.enterprise.goal.interfaces.event;

import com.enterprise.goal.application.service.GoalRecalculationService;
import com.enterprise.goal.domain.model.Goal;
import com.enterprise.goal.domain.repository.GoalRepository;
import com.enterprise.goal.infrastructure.messaging.event.GoalRecalculatedEvent;
import com.enterprise.goal.infrastructure.messaging.event.KpiProgressUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GoalEventListener {

    private final GoalRepository goalRepo;
    private final GoalRecalculationService recalculationService;

    @RabbitListener(queues = "goal-service.kpi.progress.queue")
    public void handleKpiProgressUpdated(KpiProgressUpdatedEvent event) {
        List<Goal> linkedGoals = goalRepo.findByKpiIdAndTenantId(event.getKpiId(), event.getTenantId());
        linkedGoals.forEach(goal -> recalculationService.recalculate(goal.getId(), event.getTenantId()));
    }
    
    @RabbitListener(queues = "goal-service.goal.recalculated.queue")
    public void handleGoalRecalculated(GoalRecalculatedEvent event) {
        goalRepo.findByIdAndTenantId(event.getGoalId(), event.getTenantId()).ifPresent(goal -> {
            goal.setOverallProgress(event.getNewProgress());
            goalRepo.save(goal);
            recalculationService.recalculate(goal.getId(), event.getTenantId());
        });
    }
}
