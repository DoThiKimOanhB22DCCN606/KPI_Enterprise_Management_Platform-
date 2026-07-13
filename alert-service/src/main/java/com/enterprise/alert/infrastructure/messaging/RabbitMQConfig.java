package com.enterprise.alert.infrastructure.messaging;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange.name:kemp.events.exchange}")
    private String exchangeName;

    @Value("${rabbitmq.queue.alert.kpi:alert-service.kpi.progress.queue}")
    private String kpiQueueName;
    
    @Value("${rabbitmq.queue.alert.goal:alert-service.goal.recalculated.queue}")
    private String goalQueueName;

    @Bean
    public TopicExchange eventsExchange() {
        return new TopicExchange(exchangeName);
    }

    @Value("${rabbitmq.exchange.alerts:kemp.alerts}")
    private String alertsExchangeName;

    @Bean
    public TopicExchange kempAlertsExchange() {
        return new TopicExchange(alertsExchangeName);
    }

    @Bean
    public Queue alertKpiQueue() {
        return QueueBuilder.durable(kpiQueueName).build();
    }

    @Bean
    public Queue alertGoalQueue() {
        return QueueBuilder.durable(goalQueueName).build();
    }

    @Bean
    public Binding alertKpiBinding(Queue alertKpiQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(alertKpiQueue).to(eventsExchange).with("kpi.progress.updated");
    }

    @Bean
    public Binding alertGoalBinding(Queue alertGoalQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(alertGoalQueue).to(eventsExchange).with("goal.recalculated");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
