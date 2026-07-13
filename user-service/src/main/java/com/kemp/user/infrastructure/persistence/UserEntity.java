package com.kemp.user.infrastructure.persistence;

import com.kemp.user.domain.model.User.Status;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "users")
@SQLDelete(sql = "UPDATE users SET deleted_at = CURRENT_TIMESTAMP WHERE id=?")
@SQLRestriction("deleted_at IS NULL")
@Data
public class UserEntity {
    @Id
    private UUID id;
    private UUID tenantId;
    private String email;
    private String passwordHash;
    private String fullName;
    private String avatarUrl;
    private String phone;
    private String employeeCode;
    @Enumerated(EnumType.STRING)
    private Status status;
    private OffsetDateTime lastLoginAt;
    private UUID organizationUnitId;
    private String mfaSecret;
    private Boolean mfaEnabled;
    @CreationTimestamp
    private OffsetDateTime createdAt;
    @UpdateTimestamp
    private OffsetDateTime updatedAt;
    private UUID createdBy;
    private UUID updatedBy;
    @Version
    private Long version;

    @Column(name = "deleted_at")
    private java.time.Instant deletedAt;
}
