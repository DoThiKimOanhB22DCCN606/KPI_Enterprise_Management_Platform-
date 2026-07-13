package com.kemp.user.application.dto;

import com.kemp.user.domain.model.User.Status;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
    private UUID id;
    private UUID tenantId;
    private String email;
    private String fullName;
    private String avatarUrl;
    private String phone;
    private String employeeCode;
    private Status status;
    private OffsetDateTime lastLoginAt;
    private UUID organizationUnitId;
    private Boolean mfaEnabled;
    private OffsetDateTime createdAt;
    private List<String> roles;
}
