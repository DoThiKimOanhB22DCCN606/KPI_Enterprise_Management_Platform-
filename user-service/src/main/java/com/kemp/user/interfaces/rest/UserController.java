package com.kemp.user.interfaces.rest;

import com.kemp.user.application.dto.AssignRolesRequest;
import com.kemp.user.application.dto.CreateUserRequest;
import com.kemp.user.application.dto.InviteUserRequest;
import com.kemp.user.application.dto.UpdateUserRequest;
import com.kemp.user.application.dto.UserResponse;
import com.kemp.user.application.service.UserService;
import com.kemp.user.domain.model.Role;
import com.kemp.user.domain.model.User;
import com.kemp.user.infrastructure.config.TenantContext;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // In production, the JWT Filter in API Gateway / Security Config sets this.
    @ModelAttribute
    public void setTenantContext(@RequestHeader(value = "X-Tenant-Id", required = false) String tenantId) {
        if (tenantId != null && !tenantId.trim().isEmpty()) {
            TenantContext.setTenantId(UUID.fromString(tenantId));
        } else {
            throw new IllegalArgumentException("X-Tenant-Id header is required for tenant context");
        }
    }

    @PreAuthorize("hasAnyRole('TENANT_ADMIN','HR_ADMIN')")
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        User user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(user));
    }

    @GetMapping
    public ResponseEntity<Page<UserResponse>> listUsers(
            @RequestParam(required = false) User.Status status,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<User> users = userService.listUsers(status, role, PageRequest.of(page, size));
        return ResponseEntity.ok(users.map(this::toResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(toResponse(userService.getUser(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable UUID id, @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(toResponse(userService.updateUser(id, request)));
    }

    @PreAuthorize("hasAnyRole('TENANT_ADMIN','HR_ADMIN','CEO')")
    @PutMapping("/{id}/roles")
    public ResponseEntity<UserResponse> assignRoles(@PathVariable UUID id, @Valid @RequestBody AssignRolesRequest request) {
        return ResponseEntity.ok(toResponse(userService.assignRoles(id, request.getRoleIds())));
    }

    @PreAuthorize("hasAnyRole('TENANT_ADMIN','HR_ADMIN')")
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<UserResponse> deactivateUser(@PathVariable UUID id) {
        return ResponseEntity.ok(toResponse(userService.deactivateUser(id)));
    }

    @PreAuthorize("hasAnyRole('TENANT_ADMIN','HR_ADMIN')")
    @PostMapping("/{id}/lock")
    public ResponseEntity<UserResponse> lockUser(@PathVariable UUID id) {
        return ResponseEntity.ok(toResponse(userService.lockUser(id)));
    }

    @PreAuthorize("hasAnyRole('TENANT_ADMIN','HR_ADMIN')")
    @PostMapping("/{id}/unlock")
    public ResponseEntity<UserResponse> unlockUser(@PathVariable UUID id) {
        return ResponseEntity.ok(toResponse(userService.unlockUser(id)));
    }

    @PreAuthorize("hasAnyRole('TENANT_ADMIN','HR_ADMIN')")
    @PostMapping("/invite")
    public ResponseEntity<UserResponse> inviteUser(@Valid @RequestBody InviteUserRequest request) {
        return ResponseEntity.ok(toResponse(userService.inviteUser(request)));
    }

    @PostMapping("/tenants/batch")
    public ResponseEntity<Map<UUID, UUID>> getTenantIdsForUsers(@RequestBody List<UUID> userIds) {
        return ResponseEntity.ok(userService.getTenantIdsForUsers(userIds));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@RequestHeader("X-User-Id") String userIdStr) {
        if (userIdStr == null || userIdStr.trim().isEmpty()) {
            throw new IllegalArgumentException("X-User-Id header is required");
        }
        UUID userId = UUID.fromString(userIdStr);
        return ResponseEntity.ok(toResponse(userService.getUser(userId)));
    }

    private UserResponse toResponse(User user) {
        List<String> roleCodes = user.getRoles() != null 
            ? user.getRoles().stream().map(Role::getCode).collect(Collectors.toList())
            : Collections.emptyList();
            
        return UserResponse.builder()
            .id(user.getId())
            .tenantId(user.getTenantId())
            .email(user.getEmail())
            .fullName(user.getFullName())
            .avatarUrl(user.getAvatarUrl())
            .phone(user.getPhone())
            .employeeCode(user.getEmployeeCode())
            .status(user.getStatus())
            .lastLoginAt(user.getLastLoginAt())
            .organizationUnitId(user.getOrganizationUnitId())
            .mfaEnabled(user.getMfaEnabled())
            .createdAt(user.getCreatedAt())
            .roles(roleCodes)
            .build();
    }
}
