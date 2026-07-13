package com.kemp.organization.application.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import lombok.Data;

@Data
public class UpdateOrgUnitRequest {
    @NotBlank
    private String name;
    private UUID managerUserId;
}
