package com.kemp.user.application.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data
public class AssignRolesRequest {
    @NotEmpty
    private List<UUID> roleIds;
}
