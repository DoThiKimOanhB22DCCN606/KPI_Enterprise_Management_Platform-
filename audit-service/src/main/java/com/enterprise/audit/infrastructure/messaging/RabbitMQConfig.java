package com.enterprise.audit.infrastructure.messaging;

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

    @Value("${rabbitmq.queue.audit:audit-service.logs.queue}")
    private String auditQueueName;

    @Bean
    public TopicExchange eventsExchange() {
        return new TopicExchange(exchangeName);
    }

    @Bean
    public Queue auditQueue() {
        return QueueBuilder.durable(auditQueueName).build();
    }

    /**
     * Bind the audit queue to capture ALL events with the routing key pattern "audit.#"
     */
    @Bean
    public Binding auditBinding(Queue auditQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(auditQueue).to(eventsExchange).with("audit.#");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
