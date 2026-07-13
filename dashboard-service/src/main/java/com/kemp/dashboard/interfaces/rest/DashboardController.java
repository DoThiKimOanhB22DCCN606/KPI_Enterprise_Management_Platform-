package com.kemp.dashboard.interfaces.rest;

import com.kemp.dashboard.application.dto.*;
import com.kemp.dashboard.application.service.DashboardService;
import com.kemp.dashboard.infrastructure.config.TenantContext;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/dashboards")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @ModelAttribute
    public void setMockTenantContext(
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantId,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        if (tenantId != null) {
            TenantContext.setTenantId(UUID.fromString(tenantId));
        } else {
            TenantContext.setTenantId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        }
        
        if (userId != null) {
            TenantContext.setUserId(UUID.fromString(userId));
        } else {
            TenantContext.setUserId(UUID.fromString("00000000-0000-0000-0000-000000000002"));
        }
    }

    @PostMapping
    public ResponseEntity<DashboardResponse> createDashboard(@Valid @RequestBody CreateDashboardRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dashboardService.createDashboard(request));
    }

    @GetMapping
    public ResponseEntity<Page<DashboardResponse>> listDashboards(
            @RequestParam(required = false) UUID createdBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(dashboardService.listDashboards(createdBy, PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DashboardResponse> getDashboard(@PathVariable UUID id) {
        return ResponseEntity.ok(dashboardService.getDashboard(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DashboardResponse> updateDashboard(@PathVariable UUID id, @Valid @RequestBody UpdateDashboardRequest request) {
        return ResponseEntity.ok(dashboardService.updateDashboard(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDashboard(@PathVariable UUID id) {
        dashboardService.deleteDashboard(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/share")
    public ResponseEntity<ShareDashboardResponse> shareDashboard(@PathVariable UUID id) {
        return ResponseEntity.ok(dashboardService.shareDashboard(id));
    }

    @DeleteMapping("/{id}/share")
    public ResponseEntity<Void> revokeDashboardShare(@PathVariable UUID id) {
        dashboardService.revokeDashboardShare(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/public/{token}")
    public ResponseEntity<DashboardResponse> getPublicDashboard(@PathVariable String token) {
        return ResponseEntity.ok(dashboardService.getPublicDashboard(token));
    }

    @PostMapping("/{id}/widgets")
    public ResponseEntity<DashboardResponse> addWidget(@PathVariable UUID id, @Valid @RequestBody AddWidgetRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dashboardService.addWidget(id, request));
    }

    @PutMapping("/{id}/widgets/{widgetId}")
    public ResponseEntity<DashboardResponse> updateWidget(
            @PathVariable UUID id, 
            @PathVariable UUID widgetId, 
            @Valid @RequestBody UpdateWidgetRequest request) {
        return ResponseEntity.ok(dashboardService.updateWidget(id, widgetId, request));
    }

    @DeleteMapping("/{id}/widgets/{widgetId}")
    public ResponseEntity<Void> removeWidget(@PathVariable UUID id, @PathVariable UUID widgetId) {
        dashboardService.removeWidget(id, widgetId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/layout")
    public ResponseEntity<DashboardResponse> updateLayout(@PathVariable UUID id, @Valid @RequestBody UpdateLayoutRequest request) {
        return ResponseEntity.ok(dashboardService.updateLayout(id, request));
    }
}
