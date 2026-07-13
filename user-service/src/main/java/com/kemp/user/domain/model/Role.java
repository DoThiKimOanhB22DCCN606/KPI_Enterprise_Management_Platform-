package com.kemp.user.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Role {
    private UUID id;
    private UUID tenantId;
    private String code;
    private String name;
    private String description;
    private Boolean systemRole;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private Long version;
}
