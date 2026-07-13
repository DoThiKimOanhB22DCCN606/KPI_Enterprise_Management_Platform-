package com.enterprise.bff.infrastructure.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Enumeration;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                // Pass standard security and context headers
                String auth = request.getHeader("Authorization");
                if (auth != null) requestTemplate.header("Authorization", auth);

                String tenantId = request.getHeader("X-Tenant-Id");
                if (tenantId != null) requestTemplate.header("X-Tenant-Id", tenantId);

                String userId = request.getHeader("X-User-Id");
                if (userId != null) requestTemplate.header("X-User-Id", userId);
                
                String userRoles = request.getHeader("X-User-Roles");
                if (userRoles != null) requestTemplate.header("X-User-Roles", userRoles);
            }
        };
    }
}
