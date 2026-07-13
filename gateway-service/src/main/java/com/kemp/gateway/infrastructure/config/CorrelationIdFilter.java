package com.kemp.gateway.infrastructure.config;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

@Component
public class CorrelationIdFilter implements WebFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String correlationId = Optional.ofNullable(exchange.getRequest().getHeaders().getFirst("X-Correlation-ID"))
                .orElse(UUID.randomUUID().toString());

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(builder -> builder.header("X-Correlation-ID", correlationId))
                .build();

        mutatedExchange.getResponse().getHeaders().add("X-Correlation-ID", correlationId);

        return chain.filter(mutatedExchange)
                .contextWrite(ctx -> ctx.put("correlationId", correlationId));
    }
}