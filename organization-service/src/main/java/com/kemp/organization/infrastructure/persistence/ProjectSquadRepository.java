package com.kemp.organization.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectSquadRepository extends JpaRepository<ProjectSquadEntity, UUID> {
    List<ProjectSquadEntity> findAllByTenantId(UUID tenantId);
}
