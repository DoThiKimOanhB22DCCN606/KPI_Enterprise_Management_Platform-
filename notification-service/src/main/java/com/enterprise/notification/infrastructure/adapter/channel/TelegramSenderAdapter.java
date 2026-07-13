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
public class TelegramSenderAdapter {
    private final WebClient webClient = WebClient.builder().build();

    @Value("${notification.telegram.bot-token}")
    private String botToken;

    public void send(String chatId, String message) {
        if (botToken == null || botToken.isEmpty()) {
            log.warn("Telegram bot token not configured");
            return;
        }
        String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";
        webClient.post().uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Map.of("chat_id", chatId, "text", message, "parse_mode", "HTML"))
            .retrieve().bodyToMono(Void.class)
            .timeout(Duration.ofSeconds(5))
            .onErrorResume(e -> { 
                log.error("Telegram failed: {}", e.getMessage()); 
                return Mono.empty(); 
            })
            .block();
    }
}
