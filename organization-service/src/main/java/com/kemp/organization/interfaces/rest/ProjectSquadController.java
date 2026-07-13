package com.kemp.organization.interfaces.rest;

import com.kemp.organization.infrastructure.config.TenantContext;
import com.kemp.organization.infrastructure.persistence.ProjectSquadEntity;
import com.kemp.organization.infrastructure.persistence.ProjectSquadRepository;
import com.kemp.organization.infrastructure.persistence.SquadMemberEntity;
import com.kemp.organization.infrastructure.persistence.SquadMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/project-squads")
@RequiredArgsConstructor
public class ProjectSquadController {

    private final ProjectSquadRepository projectSquadRepository;
    private final SquadMemberRepository squadMemberRepository;

    @GetMapping
    public ResponseEntity<List<ProjectSquadEntity>> listSquads() {
        UUID tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(projectSquadRepository.findAllByTenantId(tenantId));
    }

    @PostMapping
    @Transactional
    public ResponseEntity<ProjectSquadEntity> createSquad(@RequestBody ProjectSquadEntity request) {
        UUID tenantId = TenantContext.getTenantId();
        request.setId(UUID.randomUUID());
        request.setTenantId(tenantId);
        request.setStatus("ACTIVE");
        request.setCreatedAt(Instant.now());
        request.setUpdatedAt(Instant.now());
        ProjectSquadEntity saved = projectSquadRepository.save(request);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/{projectId}/members/{userId}")
    @Transactional
    public ResponseEntity<Void> addMember(@PathVariable UUID projectId, @PathVariable UUID userId) {
        squadMemberRepository.save(SquadMemberEntity.builder()
                .projectId(projectId)
                .userId(userId)
                .build());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{projectId}/members/{userId}")
    @Transactional
    public ResponseEntity<Void> removeMember(@PathVariable UUID projectId, @PathVariable UUID userId) {
        squadMemberRepository.deleteByProjectIdAndUserId(projectId, userId);
        return ResponseEntity.noContent().build();
    }
}
