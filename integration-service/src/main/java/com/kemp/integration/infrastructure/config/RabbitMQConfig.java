package com.kemp.integration.infrastructure.config;

import java.util.Map;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

@Configuration
public class RabbitMQConfig {

    public static final String IMPORT_QUEUE = "import.jobs.queue";
    public static final String IMPORT_DLQ = "import.jobs.dlq";

    @Bean
    public Queue importDlq() {
        return QueueBuilder.durable(IMPORT_DLQ).build();
    }

    @Bean
    public Queue importQueue() {
        return QueueBuilder.durable(IMPORT_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", IMPORT_DLQ)
                .withArgument("x-max-priority", 5)
                .build();
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
