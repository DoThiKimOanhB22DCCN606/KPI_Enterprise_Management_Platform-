package com.kemp.organization.domain.model;

import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrgUnit {
    private UUID id;
    private UUID tenantId;
    private UUID parentId;
    private OrgUnitType type;
    private String code;
    private String name;
    private UUID managerUserId;
    private String path;
    private Integer level;
    private Boolean active;
}
