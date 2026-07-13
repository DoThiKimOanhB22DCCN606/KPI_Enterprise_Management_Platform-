package com.kemp.user.application.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import lombok.Data;

@Data
public class UpdateUserRequest {
    @NotBlank
    private String fullName;
    private String phone;
    private String employeeCode;
    private UUID organizationUnitId;
}
