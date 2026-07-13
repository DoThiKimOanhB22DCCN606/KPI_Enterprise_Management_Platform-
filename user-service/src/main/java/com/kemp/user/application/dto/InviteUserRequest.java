package com.kemp.user.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data
public class InviteUserRequest {
    @NotBlank
    @Email
    private String email;
    private List<UUID> roleIds;
}
