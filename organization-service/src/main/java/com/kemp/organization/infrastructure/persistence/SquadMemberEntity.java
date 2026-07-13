package com.kemp.organization.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "squad_members")
@IdClass(SquadMemberEntity.SquadMemberId.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SquadMemberEntity {

    @Id
    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Id
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SquadMemberId implements Serializable {
        private UUID projectId;
        private UUID userId;
    }
}
