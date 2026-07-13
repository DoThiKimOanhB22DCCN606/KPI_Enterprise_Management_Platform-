package com.kemp.tenant.infrastructure.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Data;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "tenant_themes")
@Data
public class TenantThemeEntity {
    @Id
    private UUID id;
    private UUID tenantId;
    private String primaryColor;
    private String secondaryColor;
    private String logoUrl;
    private String faviconUrl;
    private String fontFamily;
    private String companyName;
    private String tagline;
    @UpdateTimestamp
    private OffsetDateTime updatedAt;
}
