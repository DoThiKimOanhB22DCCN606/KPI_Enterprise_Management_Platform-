package com.enterprise.ai.infrastructure.config;

import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

/**
 * Configures the OpenAI client with explicit HTTP timeouts and a
 * single-attempt retry policy so a slow or unreachable OpenAI endpoint
 * fails fast (within ~60s) instead of hanging the request thread indefinitely
 * via Spring AI's default exponential-backoff retry loop.
 */
@Configuration
public class OpenAiClientConfig {

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Value("${spring.ai.openai.base-url}")
    private String baseUrl;

    @Value("${spring.ai.openai.chat.options.model}")
    private String model;

    @Bean
    @org.springframework.context.annotation.Primary
    public OpenAiApi openAiApi() {
        // 10s connect, 60s read
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);
        factory.setReadTimeout(60_000);

        RestClient.Builder restClientBuilder = RestClient.builder()
                .requestFactory(factory);

        WebClient.Builder webClientBuilder = WebClient.builder();

        return new OpenAiApi(baseUrl, apiKey, restClientBuilder, webClientBuilder);
    }

    @Bean
    @org.springframework.context.annotation.Primary
    public OpenAiChatModel openAiChatModel(OpenAiApi openAiApi) {
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .withModel(model)
                .withTemperature(0.1f)
                .build();

        // maxAttempts=1 means no retries: first failure surfaces immediately
        RetryTemplate retryTemplate = RetryTemplate.builder()
                .maxAttempts(1)
                .build();

        return new OpenAiChatModel(openAiApi, options, null, retryTemplate);
    }
}
