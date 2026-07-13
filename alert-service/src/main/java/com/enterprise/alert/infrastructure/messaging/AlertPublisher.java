package com.enterprise.alert.infrastructure.messaging;

import com.enterprise.alert.domain.event.AlertTriggeredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlertPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.name:kemp.events.exchange}")
    private String exchangeName;

    @Value("${rabbitmq.exchange.alerts:kemp.alerts}")
    private String alertsExchangeName;

    private static final String ROUTING_KEY = "alert.triggered";
    private static final String RESOLVED_ROUTING_KEY = "alert.resolved";
    private static final String KPI_DROPPED_ROUTING_KEY = "alert.kpi.dropped";

    public void publishAlert(AlertTriggeredEvent event) {
        log.info("Publishing AlertTriggeredEvent to RabbitMQ: {}", event.getEventId());
        rabbitTemplate.convertAndSend(exchangeName, ROUTING_KEY, event);
    }

    public void publishKpiDropped(com.enterprise.alert.domain.event.KpiDroppedEvent event) {
        log.info("Publishing KpiDroppedEvent to RabbitMQ: {}", event.getEventId());
        rabbitTemplate.convertAndSend(alertsExchangeName, KPI_DROPPED_ROUTING_KEY, event);
    }

    public void publishAlertResolved(com.enterprise.alert.domain.event.AlertResolvedEvent event) {
        log.info("Publishing AlertResolvedEvent to RabbitMQ: {}", event.getEventId());
        rabbitTemplate.convertAndSend(exchangeName, RESOLVED_ROUTING_KEY, event);
    }
}
