package com.enterprise.audit.application.service;

import com.enterprise.audit.infrastructure.client.UserClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditTenantBackfillTask {

    private final JdbcTemplate jdbcTemplate;
    private final UserClient userClient;
    private static final String FALLBACK_TENANT = "00000000-0000-0000-0000-000000000001";

    @Scheduled(initialDelay = 5000, fixedDelay = 86400000) // Runs shortly after startup, then daily
    @SchedulerLock(name = "auditTenantBackfill", lockAtMostFor = "10m", lockAtLeastFor = "1m")
    public void backfillTenantIds() {
        log.info("Starting audit tenant backfill task");

        // Find distinct user IDs that have the fallback tenant
        String sql = "SELECT DISTINCT user_id FROM audit_logs WHERE tenant_id = ? AND user_id IS NOT NULL";
        List<UUID> userIds = jdbcTemplate.queryForList(sql, UUID.class, UUID.fromString(FALLBACK_TENANT));

        if (userIds.isEmpty()) {
            log.info("No audit records found requiring tenant backfill.");
            return;
        }

        log.info("Found {} users with audit records requiring tenant backfill. Fetching mapping from user-service...", userIds.size());
        
        try {
            // Fetch mappings
            Map<UUID, UUID> userToTenant = userClient.getTenantMappings(userIds);
            
            // Group by tenantId to batch update
            Map<UUID, List<UUID>> usersByTenant = userToTenant.entrySet().stream()
                .filter(e -> e.getValue() != null && e.getKey() != null)
                .collect(Collectors.groupingBy(
                    Map.Entry::getValue,
                    Collectors.mapping(Map.Entry::getKey, Collectors.toList())
                ));

            // Update database
            String updateSql = "UPDATE audit_logs SET tenant_id = ? WHERE user_id = ? AND tenant_id = ?";
            int totalUpdated = 0;
            
            for (Map.Entry<UUID, List<UUID>> entry : usersByTenant.entrySet()) {
                UUID actualTenantId = entry.getKey();
                List<UUID> users = entry.getValue();
                
                List<Object[]> batchArgs = users.stream()
                    .map(u -> new Object[]{actualTenantId, u, UUID.fromString(FALLBACK_TENANT)})
                    .collect(Collectors.toList());
                    
                int[] updated = jdbcTemplate.batchUpdate(updateSql, batchArgs);
                totalUpdated += java.util.Arrays.stream(updated).sum();
            }
            
            log.info("Successfully backfilled {} audit records with correct tenant IDs.", totalUpdated);

        } catch (Exception e) {
            log.error("Failed to backfill audit tenant IDs", e);
        }
    }
}
