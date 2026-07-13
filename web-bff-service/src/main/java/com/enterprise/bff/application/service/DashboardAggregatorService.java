package com.enterprise.bff.application.service;

import com.enterprise.bff.application.dto.DashboardViewDTO;
import com.enterprise.bff.application.dto.KpiSummaryDTO;
import com.enterprise.bff.application.dto.OrgDTO;
import com.enterprise.bff.application.dto.UserDTO;
import com.enterprise.bff.infrastructure.client.KpiClient;
import com.enterprise.bff.infrastructure.client.OrganizationClient;
import com.enterprise.bff.infrastructure.client.UserClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.RequestAttributes;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardAggregatorService {

    private final UserClient userClient;
    private final OrganizationClient organizationClient;
    private final KpiClient kpiClient;

    public DashboardViewDTO getDashboardData(UUID userId, UUID orgId) {
        
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        
        // 1. Fire parallel async requests
        CompletableFuture<UserDTO> userFuture = CompletableFuture.supplyAsync(() -> {
            RequestContextHolder.setRequestAttributes(attributes);
            try { return fetchUser(userId); } finally { RequestContextHolder.resetRequestAttributes(); }
        });
        CompletableFuture<OrgDTO> orgFuture = CompletableFuture.supplyAsync(() -> {
            RequestContextHolder.setRequestAttributes(attributes);
            try { return fetchOrganization(orgId); } finally { RequestContextHolder.resetRequestAttributes(); }
        });
        CompletableFuture<List<KpiSummaryDTO>> kpiFuture = CompletableFuture.supplyAsync(() -> {
            RequestContextHolder.setRequestAttributes(attributes);
            try { return fetchKpis(userId); } finally { RequestContextHolder.resetRequestAttributes(); }
        });

        // 2. Wait for all to complete
        CompletableFuture.allOf(userFuture, orgFuture, kpiFuture).join();

        // 3. Assemble response
        try {
            UserDTO user = userFuture.get();
            OrgDTO org = orgFuture.get();
            List<KpiSummaryDTO> kpis = kpiFuture.get();
            
            BigDecimal overallProgress = calculateOverallProgress(kpis);

            return DashboardViewDTO.builder()
                    .user(user)
                    .organization(org)
                    .kpis(kpis)
                    .overallProgress(overallProgress)
                    .build();

        } catch (Exception e) {
            log.error("Failed to aggregate dashboard data", e);
            throw new RuntimeException("Aggregation failed", e);
        }
    }

    // Resilience bindings: Fallbacks ensure partial failure resilience
    
    @CircuitBreaker(name = "userService", fallbackMethod = "userFallback")
    protected UserDTO fetchUser(UUID userId) {
        return userClient.getUserById(userId);
    }

    @CircuitBreaker(name = "orgService", fallbackMethod = "orgFallback")
    protected OrgDTO fetchOrganization(UUID orgId) {
        return organizationClient.getOrganizationById(orgId);
    }

    @CircuitBreaker(name = "kpiService", fallbackMethod = "kpiFallback")
    protected List<KpiSummaryDTO> fetchKpis(UUID ownerId) {
        return kpiClient.getKpisByOwner(ownerId);
    }

    // Fallbacks returning empty/default objects so the UI doesn't crash completely
    
    public UserDTO userFallback(UUID userId, Throwable t) {
        log.warn("User service unavailable, returning fallback user for {}", userId);
        return UserDTO.builder().id(userId).fullName("Unavailable").build();
    }

    public OrgDTO orgFallback(UUID orgId, Throwable t) {
        log.warn("Org service unavailable, returning fallback org for {}", orgId);
        return OrgDTO.builder().id(orgId).name("Unavailable").build();
    }

    public List<KpiSummaryDTO> kpiFallback(UUID ownerId, Throwable t) {
        log.warn("KPI service unavailable, returning empty KPI list for {}", ownerId);
        return Collections.emptyList();
    }

    private BigDecimal calculateOverallProgress(List<KpiSummaryDTO> kpis) {
        if (kpis == null || kpis.isEmpty()) return BigDecimal.ZERO;
        
        BigDecimal total = kpis.stream()
                .map(k -> {
                    if (k.getTarget().compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
                    return k.getCurrentProgress().divide(k.getTarget(), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
        return total.divide(BigDecimal.valueOf(kpis.size()), 2, RoundingMode.HALF_UP);
    }
}
