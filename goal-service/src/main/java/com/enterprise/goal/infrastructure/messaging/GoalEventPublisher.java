package com.enterprise.goal.infrastructure.messaging;

import com.enterprise.goal.infrastructure.messaging.event.GoalRecalculatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GoalEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishGoalRecalculated(UUID goalId, UUID tenantId, double parentProgress) {
        GoalRecalculatedEvent event = new GoalRecalculatedEvent(goalId, tenantId, parentProgress);
        rabbitTemplate.convertAndSend("goal-service.goal.recalculated.queue", event);
    }
}
