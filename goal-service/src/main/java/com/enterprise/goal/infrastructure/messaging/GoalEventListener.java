package com.enterprise.goal.infrastructure.messaging;

import com.enterprise.goal.application.service.GoalRecalculationService;
import com.enterprise.goal.domain.model.Goal;
import com.enterprise.goal.domain.repository.GoalRepository;
import com.enterprise.goal.infrastructure.messaging.event.GoalRecalculatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoalEventListener {

    private final GoalRepository goalRepo;
    private final GoalRecalculationService recalculationService;

    @RabbitListener(queues = "${rabbitmq.queue.goal.recalculated:goal-service.goal.recalculated.queue}")
    public void handleGoalRecalculated(GoalRecalculatedEvent event) {
        log.info("Received GoalRecalculatedEvent for Goal ID: {}", event.getGoalId());

        Goal parent = goalRepo.findByIdAndTenantId(event.getGoalId(), event.getTenantId())
                .orElse(null);

        if (parent == null) {
            log.warn("Goal not found for ID: {}. Skipping.", event.getGoalId());
            return;
        }

        parent.setOverallProgress(event.getNewProgress());
        parent.setCurrentValue(parent.getTargetValue() * parent.getOverallProgress() / 100.0);
        goalRepo.save(parent);

        log.info("Updated Goal ID: {} with overallProgress: {}", parent.getId(), parent.getOverallProgress());

        // Propagate roll-up to the next parent
        recalculationService.calculateAndPublishParentProgress(parent, event.getTenantId());
    }
}
