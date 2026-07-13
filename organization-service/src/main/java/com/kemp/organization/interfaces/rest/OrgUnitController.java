package com.kemp.organization.interfaces.rest;

import com.kemp.organization.application.dto.CreateOrgUnitRequest;
import com.kemp.organization.application.dto.MoveOrgUnitRequest;
import com.kemp.organization.application.dto.OrgUnitResponse;
import com.kemp.organization.application.dto.TransferRequest;
import com.kemp.organization.application.dto.UpdateOrgUnitRequest;
import com.kemp.organization.application.service.OrgUnitService;
import com.kemp.organization.domain.model.OrgUnit;
import com.kemp.organization.domain.model.OrgUnitType;
import com.kemp.organization.infrastructure.config.TenantContext;
import jakarta.validation.Valid;
import java.util.ArrayList;
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
@RequestMapping("/v1/organizations")
@RequiredArgsConstructor
public class OrgUnitController {

    private final OrgUnitService orgUnitService;

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
    public ResponseEntity<OrgUnitResponse> createUnit(@Valid @RequestBody CreateOrgUnitRequest request) {
        OrgUnit unit = orgUnitService.createUnit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(unit, null));
    }

    @GetMapping
    public ResponseEntity<Page<OrgUnitResponse>> listUnits(
            @RequestParam(required = false) OrgUnitType type,
            @RequestParam(required = false) UUID parentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<OrgUnit> units = orgUnitService.listUnits(type, parentId, PageRequest.of(page, size));
        return ResponseEntity.ok(units.map(u -> toResponse(u, null)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrgUnitResponse> getUnit(@PathVariable UUID id) {
        return ResponseEntity.ok(toResponse(orgUnitService.getUnit(id), null));
    }

    @GetMapping("/{id}/tree")
    public ResponseEntity<OrgUnitResponse> getSubtree(@PathVariable UUID id) {
        List<OrgUnit> flatTree = orgUnitService.getSubtree(id);
        if (flatTree.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        OrgUnit root = flatTree.stream().filter(u -> u.getId().equals(id)).findFirst()
            .orElseThrow(() -> new RuntimeException("Root not found in subtree"));
        
        return ResponseEntity.ok(buildTree(root, flatTree));
    }

    @PreAuthorize("hasAnyRole('TENANT_ADMIN','HR_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<OrgUnitResponse> updateUnit(@PathVariable UUID id, @Valid @RequestBody UpdateOrgUnitRequest request) {
        return ResponseEntity.ok(toResponse(orgUnitService.updateUnit(id, request), null));
    }

    @PreAuthorize("hasAnyRole('TENANT_ADMIN','HR_ADMIN')")
    @PostMapping("/{id}/move")
    public ResponseEntity<OrgUnitResponse> moveUnit(@PathVariable UUID id, @RequestBody MoveOrgUnitRequest request) {
        return ResponseEntity.ok(toResponse(orgUnitService.moveUnit(id, request.getNewParentId()), null));
    }

    @PreAuthorize("hasAnyRole('TENANT_ADMIN','HR_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable UUID id) {
        orgUnitService.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('TENANT_ADMIN','HR_ADMIN')")
    @PostMapping("/{unitId}/transfer-member")
    public ResponseEntity<Map<String, String>> transferMember(@PathVariable UUID unitId, @Valid @RequestBody TransferRequest request) {
        // In a real implementation this would publish an event or call user-service.
        // For now, we simulate the action.
        return ResponseEntity.ok(Map.of("status", "Transferred successfully", "userId", request.getUserId().toString(), "targetUnitId", unitId.toString()));
    }

    private OrgUnitResponse toResponse(OrgUnit unit, List<OrgUnitResponse> children) {
        return OrgUnitResponse.builder()
            .id(unit.getId())
            .tenantId(unit.getTenantId())
            .parentId(unit.getParentId())
            .type(unit.getType())
            .code(unit.getCode())
            .name(unit.getName())
            .managerUserId(unit.getManagerUserId())
            .path(unit.getPath())
            .level(unit.getLevel())
            .active(unit.getActive())
            .children(children)
            .build();
    }

    private OrgUnitResponse buildTree(OrgUnit node, List<OrgUnit> flatTree) {
        List<OrgUnitResponse> children = flatTree.stream()
            .filter(u -> node.getId().equals(u.getParentId()))
            .map(u -> buildTree(u, flatTree))
            .collect(Collectors.toList());
        return toResponse(node, children);
    }
}
