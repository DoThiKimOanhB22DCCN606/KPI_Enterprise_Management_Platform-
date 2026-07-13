package com.kemp.organization.application.dto;

import com.kemp.organization.domain.model.OrgUnitType;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrgUnitResponse {
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
    private List<OrgUnitResponse> children;
}
