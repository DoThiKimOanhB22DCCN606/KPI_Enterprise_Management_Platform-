package com.kemp.tenant.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Tenant {
    private UUID id;
    private String code;
    private String name;
    private String logoUrl;
    private String status;
    private String timezone;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private Long version;
}
