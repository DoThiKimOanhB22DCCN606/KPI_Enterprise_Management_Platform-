package com.kemp.tenant.infrastructure.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    @Bean
    public TopicExchange tenantExchange() {
        return new TopicExchange("tenant.exchange");
    }
}
