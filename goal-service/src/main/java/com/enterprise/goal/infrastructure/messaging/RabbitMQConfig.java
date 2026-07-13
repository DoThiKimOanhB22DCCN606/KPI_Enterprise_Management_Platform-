package com.enterprise.goal.infrastructure.messaging;

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

    @Value("${rabbitmq.queue.kpi.progress:goal-service.kpi.progress.queue}")
    private String queueName;

    @Bean
    public TopicExchange eventsExchange() {
        return new TopicExchange(exchangeName);
    }

    @Bean
    public Queue kpiProgressQueue() {
        return QueueBuilder.durable(queueName)
                .withArgument("x-dead-letter-exchange", exchangeName)
                .withArgument("x-dead-letter-routing-key", queueName + ".dlq")
                .build();
    }

    @Bean
    public Binding kpiProgressBinding(Queue kpiProgressQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(kpiProgressQueue).to(eventsExchange).with("kpi.progress.updated");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
