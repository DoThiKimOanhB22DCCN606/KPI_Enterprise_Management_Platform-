package com.kemp.organization.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SquadMemberRepository extends JpaRepository<SquadMemberEntity, SquadMemberEntity.SquadMemberId> {
    List<SquadMemberEntity> findAllByProjectId(UUID projectId);
    void deleteByProjectIdAndUserId(UUID projectId, UUID userId);
}
