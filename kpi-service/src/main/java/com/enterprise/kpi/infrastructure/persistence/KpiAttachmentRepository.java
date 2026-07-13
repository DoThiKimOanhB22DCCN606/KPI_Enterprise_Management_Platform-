package com.enterprise.kpi.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface KpiAttachmentRepository extends JpaRepository<KpiAttachmentEntity, UUID> {
    List<KpiAttachmentEntity> findByValueIdAndTenantId(UUID valueId, UUID tenantId);
}
