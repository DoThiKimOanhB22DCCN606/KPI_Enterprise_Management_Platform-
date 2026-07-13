package com.kemp.integration.infrastructure.webhook;

import com.kemp.integration.domain.model.WebhookSubscription;
import java.nio.charset.StandardCharsets;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import java.util.Base64;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookDispatcher {

    private final RestTemplate restTemplate;

    @Retryable(
        value = {RestClientException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public void dispatch(WebhookSubscription subscription, String eventType, Object payload) {
        if (!subscription.getActive()) return;
        
        try {
            String jsonPayload = "{\"event\":\"" + eventType + "\"}"; // Simplified for skeleton
            String signature = generateHmac(jsonPayload, subscription.getSecretHash());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-KEMP-Signature", signature);
            
            HttpEntity<String> request = new HttpEntity<>(jsonPayload, headers);
            
            log.info("Dispatching webhook to {} for event {}", subscription.getUrl(), eventType);
            restTemplate.postForEntity(subscription.getUrl(), request, Void.class);
            log.info("Webhook successfully dispatched to {}", subscription.getUrl());
            
        } catch (Exception e) {
            if (e instanceof RestClientException) {
                log.warn("Network error during webhook dispatch to {}: {}", subscription.getUrl(), e.getMessage());
                throw (RestClientException) e; // Throw to trigger @Retryable
            }
            log.error("Internal error processing webhook payload: {}", e.getMessage());
        }
    }

    @Recover
    public void recover(RestClientException e, WebhookSubscription subscription, String eventType, Object payload) {
        log.error("Failed to dispatch webhook to {} after 3 retries. Event: {}. Error: {}", 
            subscription.getUrl(), eventType, e.getMessage());
        // Here we could update the subscription status to "FAILING" in the DB
    }

    private String generateHmac(String data, String secret) throws Exception {
        Mac sha256HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256HMAC.init(secretKey);
        return Base64.getEncoder().encodeToString(sha256HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }
}
