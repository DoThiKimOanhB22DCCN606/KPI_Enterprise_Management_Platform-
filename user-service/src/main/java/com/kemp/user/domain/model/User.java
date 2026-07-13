package com.kemp.user.domain.model;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class User {
    private UUID id;
    private UUID tenantId;
    private String email;
    private String passwordHash;
    private String fullName;
    private String avatarUrl;
    private String phone;
    private String employeeCode;
    private Status status;
    private OffsetDateTime lastLoginAt;
    private UUID organizationUnitId;
    private String mfaSecret;
    private Boolean mfaEnabled;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private UUID createdBy;
    private UUID updatedBy;
    private Long version;
    private List<Role> roles;

    public enum Status {
        ACTIVE, INACTIVE, LOCKED, INVITED, RESIGNED, TERMINATED, SUSPENDED
    }
}
