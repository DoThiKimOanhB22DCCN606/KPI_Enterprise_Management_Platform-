package com.enterprise.ai.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface AiConversationRepository extends JpaRepository<AiConversationEntity, UUID> {
    List<AiConversationEntity> findByUserIdAndTenantIdOrderByUpdatedAtDesc(UUID userId, UUID tenantId);
}
