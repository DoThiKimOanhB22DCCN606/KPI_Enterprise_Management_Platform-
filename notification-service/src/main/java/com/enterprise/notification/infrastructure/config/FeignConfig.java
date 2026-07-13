package com.enterprise.notification.infrastructure.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            if (TenantContext.getTenantId() != null) {
                requestTemplate.header("X-Tenant-Id", TenantContext.getTenantId().toString());
            }
        };
    }
}
