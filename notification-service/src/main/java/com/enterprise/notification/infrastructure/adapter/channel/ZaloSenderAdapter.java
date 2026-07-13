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
public class ZaloSenderAdapter {
    private final WebClient webClient = WebClient.builder().build();

    @Value("${notification.zalo.access-token}")
    private String zaloAccessToken;

    public void send(String userId, String message) {
        if (zaloAccessToken == null || zaloAccessToken.isEmpty()) {
            log.warn("Zalo access token not configured");
            return;
        }
        webClient.post()
            .uri("https://openapi.zalo.me/v2.0/oa/message")
            .header("access_token", zaloAccessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Map.of("recipient", Map.of("user_id", userId),
                              "message", Map.of("text", message)))
            .retrieve().bodyToMono(Void.class)
            .timeout(Duration.ofSeconds(5))
            .onErrorResume(e -> { 
                log.error("Zalo failed: {}", e.getMessage()); 
                return Mono.empty(); 
            })
            .block();
    }
}
