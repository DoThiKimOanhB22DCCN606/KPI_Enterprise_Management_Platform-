package com.enterprise.notification.infrastructure.adapter.messaging;

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

    @Value("${rabbitmq.queue.notification.alert:notification-service.alert.queue}")
    private String alertQueueName;

    @Bean
    public TopicExchange eventsExchange() {
        return new TopicExchange(exchangeName);
    }

    @Bean
    public Queue alertQueue() {
        return QueueBuilder.durable(alertQueueName).build();
    }

    @Bean
    public Binding alertBinding(Queue alertQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(alertQueue).to(eventsExchange).with("alert.triggered");
    }

    @Value("${rabbitmq.exchange.alerts:kemp.alerts}")
    private String alertsExchangeName;
    
    @Value("${rabbitmq.exchange.alerts.dlx:kemp.alerts.dlx}")
    private String alertsDlxName;

    @Value("${rabbitmq.queue.notification.kpi-dropped:notification-service.alert.kpi-dropped}")
    private String kpiDroppedQueueName;
    
    @Value("${rabbitmq.queue.notification.kpi-dropped.dlq:notification-service.alert.kpi-dropped.dlq}")
    private String kpiDroppedDlqName;

    @Bean
    public TopicExchange alertsExchange() {
        return new TopicExchange(alertsExchangeName);
    }
    
    @Bean
    public TopicExchange alertsDlx() {
        return new TopicExchange(alertsDlxName);
    }

    @Bean
    public Queue kpiDroppedQueue() {
        return QueueBuilder.durable(kpiDroppedQueueName)
                .withArgument("x-dead-letter-exchange", alertsDlxName)
                .withArgument("x-dead-letter-routing-key", "alert.kpi.dropped.dlq")
                .build();
    }
    
    @Bean
    public Queue kpiDroppedDlq() {
        return QueueBuilder.durable(kpiDroppedDlqName).build();
    }

    @Bean
    public Binding kpiDroppedBinding(Queue kpiDroppedQueue, TopicExchange alertsExchange) {
        return BindingBuilder.bind(kpiDroppedQueue).to(alertsExchange).with("alert.kpi.dropped");
    }
    
    @Bean
    public Binding kpiDroppedDlqBinding(Queue kpiDroppedDlq, TopicExchange alertsDlx) {
        return BindingBuilder.bind(kpiDroppedDlq).to(alertsDlx).with("alert.kpi.dropped.dlq");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
