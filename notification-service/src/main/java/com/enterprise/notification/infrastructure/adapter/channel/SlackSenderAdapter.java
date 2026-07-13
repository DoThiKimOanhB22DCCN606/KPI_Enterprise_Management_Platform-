package com.enterprise.notification.infrastructure.adapter.channel;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@Slf4j
@Component
public class SlackSenderAdapter {
    private final WebClient webClient = WebClient.builder().build();

    @Value("${notification.slack.webhook-url}")
    private String slackWebhookUrl;

    public void send(String message) {
        if (slackWebhookUrl == null || slackWebhookUrl.isEmpty()) {
            log.warn("Slack webhook URL not configured");
            return;
        }
        webClient.post()
            .uri(slackWebhookUrl)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Map.of("text", message))
            .retrieve()
            .bodyToMono(Void.class)
            .timeout(Duration.ofSeconds(5))
            .onErrorResume(e -> { 
                log.error("Slack failed: {}", e.getMessage()); 
                return Mono.empty(); 
            })
            .block();
    }
}
