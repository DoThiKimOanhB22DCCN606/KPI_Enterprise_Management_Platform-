package com.enterprise.audit.application.service;

import com.enterprise.audit.application.dto.AuditEventDTO;
import com.enterprise.audit.infrastructure.client.UserClient;
import com.enterprise.audit.infrastructure.persistence.AuditLogEntity;
import com.enterprise.audit.infrastructure.persistence.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserClient userClient;
    private static final UUID FALLBACK_TENANT = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Transactional
    public void recordEvent(AuditEventDTO event) {
        log.info("Recording audit event: {} for actor: {} on resource: {}", 
                 event.getEventType(), event.getActorId(), event.getResourceId());

        UUID actorUuid = event.getActorId() != null ? UUID.fromString(event.getActorId()) : null;
        UUID resourceUuid = event.getResourceId() != null ? UUID.fromString(event.getResourceId()) : null;
        
        UUID tenantId = null;
        if (actorUuid != null) {
            Map<UUID, UUID> mappings = userClient.getTenantMappings(Collections.singletonList(actorUuid));
            tenantId = mappings.get(actorUuid);
        }
        if (tenantId == null) {
            log.warn("Could not resolve tenantId for actor {}, this might violate DB constraints if V4 is applied", actorUuid);
            tenantId = FALLBACK_TENANT; // Might throw if V4 constraint is enforced
        }

        AuditLogEntity entity = AuditLogEntity.builder()
                .id(event.getEventId() != null ? UUID.fromString(event.getEventId()) : UUID.randomUUID())
                .tenantId(tenantId)
                .eventType(event.getEventType())
                .actorId(actorUuid)
                .resourceId(resourceUuid)
                .changes(event.getChanges())
                .timestamp(Instant.ofEpochMilli(event.getTimestamp()).atOffset(ZoneOffset.UTC))
                .build();

        auditLogRepository.save(entity);
    }
}
