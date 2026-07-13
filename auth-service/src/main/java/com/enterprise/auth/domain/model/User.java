package com.enterprise.auth.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private UUID id;
    private UUID tenantId;
    private String email;
    private String passwordHash;
    private String fullName;
    private String status;
    private java.util.List<String> roles;
    private boolean mfaEnabled;
    private String mfaSecret;
    private int failedLoginAttempts;
    private java.time.Instant lockedUntil;
}
