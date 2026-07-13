package com.kemp.organization.application.dto;

import com.kemp.organization.domain.model.OrgUnitType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Data;

@Data
public class CreateOrgUnitRequest {
    private UUID parentId;
    @NotNull
    private OrgUnitType type;
    @NotBlank
    private String code;
    @NotBlank
    private String name;
    private UUID managerUserId;
}
