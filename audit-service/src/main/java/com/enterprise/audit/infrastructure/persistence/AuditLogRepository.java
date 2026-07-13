package com.enterprise.audit.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogEntity, UUID> {

    Page<AuditLogEntity> findByTenantIdAndEventType(UUID tenantId, String eventType, Pageable pageable);

    Page<AuditLogEntity> findByTenantIdAndActorId(UUID tenantId, UUID actorId, Pageable pageable);

    Page<AuditLogEntity> findByTenantIdAndTimestampBetween(UUID tenantId, OffsetDateTime from, OffsetDateTime to, Pageable pageable);

    Page<AuditLogEntity> findByTenantIdAndEventTypeAndActorId(UUID tenantId, String eventType, UUID actorId, Pageable pageable);

    Page<AuditLogEntity> findByTenantId(UUID tenantId, Pageable pageable);
}
