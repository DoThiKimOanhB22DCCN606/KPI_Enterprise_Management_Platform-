package com.kemp.organization.application.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Data;

@Data
public class TransferRequest {
    private UUID targetUnitId;
    @NotNull
    private UUID userId;
}
