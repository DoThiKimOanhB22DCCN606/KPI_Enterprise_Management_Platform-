package com.kemp.tenant.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TenantTheme {
    private UUID id;
    private UUID tenantId;
    private String primaryColor;
    private String secondaryColor;
    private String logoUrl;
    private String faviconUrl;
    private String fontFamily;
    private String companyName;
    private String tagline;
    private OffsetDateTime updatedAt;
}
