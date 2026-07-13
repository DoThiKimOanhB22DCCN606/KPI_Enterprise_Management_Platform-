package com.kemp.organization.application.dto;

import java.util.UUID;
import lombok.Data;

@Data
public class MoveOrgUnitRequest {
    private UUID newParentId;
}
