package com.kemp.dashboard.application.service;

import com.kemp.dashboard.application.dto.*;
import com.kemp.dashboard.domain.model.Dashboard;
import com.kemp.dashboard.domain.model.DashboardWidget;
import com.kemp.dashboard.domain.repository.DashboardRepository;
import com.kemp.dashboard.domain.repository.WidgetRepository;
import com.kemp.dashboard.infrastructure.cache.DashboardCacheService;
import com.kemp.dashboard.infrastructure.config.TenantContext;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final DashboardRepository dashboardRepository;
    private final WidgetRepository widgetRepository;
    private final DashboardCacheService cacheService;

    @Transactional
    public DashboardResponse createDashboard(CreateDashboardRequest request) {
        Dashboard dashboard = Dashboard.builder()
            .tenantId(getTenantId())
            .name(request.getName())
            .description(request.getDescription())
            .fullscreenEnabled(request.getFullscreenEnabled() != null ? request.getFullscreenEnabled() : false)
            .autoRotate(request.getAutoRotate() != null ? request.getAutoRotate() : false)
            .autoRotateInterval(request.getAutoRotateInterval() != null ? request.getAutoRotateInterval() : 30)
            .createdBy(getUserId())
            .layoutJson(request.getLayoutJson() != null ? request.getLayoutJson() : "{}")
            .build();
            
        dashboard = dashboardRepository.save(dashboard);
        return toResponse(dashboard, List.of());
    }

    public Page<DashboardResponse> listDashboards(UUID createdBy, Pageable pageable) {
        Page<Dashboard> dashboards = dashboardRepository.findAllByTenantIdAndCreatedBy(getTenantId(), createdBy, pageable);
        return dashboards.map(d -> {
            List<DashboardWidget> widgets = widgetRepository.findByDashboardId(d.getId());
            return toResponse(d, widgets);
        });
    }

    public DashboardResponse getDashboard(UUID id) {
        DashboardResponse cached = cacheService.get(id);
        if (cached != null) return cached;
        
        Dashboard dashboard = dashboardRepository.findByIdAndTenantId(id, getTenantId())
                .orElseThrow(() -> new RuntimeException("Dashboard not found"));
                
        List<DashboardWidget> widgets = widgetRepository.findByDashboardId(id);
        DashboardResponse response = toResponse(dashboard, widgets);
        cacheService.put(id, response);
        return response;
    }
    
    public DashboardResponse getPublicDashboard(String token) {
        Dashboard dashboard = dashboardRepository.findByPublicToken(token)
                .orElseThrow(() -> new RuntimeException("Dashboard not found or token invalid"));
                
        List<DashboardWidget> widgets = widgetRepository.findByDashboardId(dashboard.getId());
        return toResponse(dashboard, widgets);
    }

    @Transactional
    public DashboardResponse updateDashboard(UUID id, UpdateDashboardRequest request) {
        Dashboard dashboard = dashboardRepository.findByIdAndTenantId(id, getTenantId())
                .orElseThrow(() -> new RuntimeException("Dashboard not found"));
                
        dashboard.setName(request.getName());
        dashboard.setDescription(request.getDescription());
        dashboard.setFullscreenEnabled(request.getFullscreenEnabled());
        dashboard.setAutoRotate(request.getAutoRotate());
        dashboard.setAutoRotateInterval(request.getAutoRotateInterval());
        if (request.getLayoutJson() != null) {
            dashboard.setLayoutJson(request.getLayoutJson());
        }
        
        dashboardRepository.save(dashboard);
        cacheService.evict(id);
        
        List<DashboardWidget> widgets = widgetRepository.findByDashboardId(id);
        return toResponse(dashboard, widgets);
    }

    @Transactional
    public void deleteDashboard(UUID id) {
        Dashboard dashboard = dashboardRepository.findByIdAndTenantId(id, getTenantId())
                .orElseThrow(() -> new RuntimeException("Dashboard not found"));
        widgetRepository.deleteByDashboardId(id);
        dashboardRepository.deleteById(id);
        cacheService.evict(id);
    }

    @Transactional
    public ShareDashboardResponse shareDashboard(UUID id) {
        Dashboard dashboard = dashboardRepository.findByIdAndTenantId(id, getTenantId())
                .orElseThrow(() -> new RuntimeException("Dashboard not found"));
                
        if (dashboard.getPublicToken() == null) {
            dashboard.setPublicToken(UUID.randomUUID().toString());
            dashboardRepository.save(dashboard);
            cacheService.evict(id);
        }
        
        return ShareDashboardResponse.builder()
            .publicToken(dashboard.getPublicToken())
            .shareUrl("/public/dashboards/" + dashboard.getPublicToken())
            .build();
    }

    @Transactional
    public void revokeDashboardShare(UUID id) {
        Dashboard dashboard = dashboardRepository.findByIdAndTenantId(id, getTenantId())
                .orElseThrow(() -> new RuntimeException("Dashboard not found"));
                
        dashboard.setPublicToken(null);
        dashboardRepository.save(dashboard);
        cacheService.evict(id);
    }

    @Transactional
    public DashboardResponse addWidget(UUID dashboardId, AddWidgetRequest request) {
        Dashboard dashboard = dashboardRepository.findByIdAndTenantId(dashboardId, getTenantId())
                .orElseThrow(() -> new RuntimeException("Dashboard not found"));
                
        DashboardWidget widget = DashboardWidget.builder()
            .dashboardId(dashboardId)
            .widgetType(request.getWidgetType())
            .title(request.getTitle())
            .x(request.getX() != null ? request.getX() : 0)
            .y(request.getY() != null ? request.getY() : 0)
            .width(request.getWidth() != null ? request.getWidth() : 2)
            .height(request.getHeight() != null ? request.getHeight() : 2)
            .configJson(request.getConfigJson())
            .build();
            
        widgetRepository.save(widget);
        cacheService.evict(dashboardId);
        
        List<DashboardWidget> widgets = widgetRepository.findByDashboardId(dashboardId);
        return toResponse(dashboard, widgets);
    }

    @Transactional
    public DashboardResponse updateWidget(UUID dashboardId, UUID widgetId, UpdateWidgetRequest request) {
        Dashboard dashboard = dashboardRepository.findByIdAndTenantId(dashboardId, getTenantId())
                .orElseThrow(() -> new RuntimeException("Dashboard not found"));
                
        DashboardWidget widget = widgetRepository.findById(widgetId)
                .orElseThrow(() -> new RuntimeException("Widget not found"));
                
        if (!widget.getDashboardId().equals(dashboardId)) {
            throw new RuntimeException("Widget does not belong to this dashboard");
        }
        
        if (request.getTitle() != null) widget.setTitle(request.getTitle());
        if (request.getX() != null) widget.setX(request.getX());
        if (request.getY() != null) widget.setY(request.getY());
        if (request.getWidth() != null) widget.setWidth(request.getWidth());
        if (request.getHeight() != null) widget.setHeight(request.getHeight());
        if (request.getConfigJson() != null) widget.setConfigJson(request.getConfigJson());
        
        widgetRepository.save(widget);
        cacheService.evict(dashboardId);
        
        List<DashboardWidget> widgets = widgetRepository.findByDashboardId(dashboardId);
        return toResponse(dashboard, widgets);
    }

    @Transactional
    public void removeWidget(UUID dashboardId, UUID widgetId) {
        Dashboard dashboard = dashboardRepository.findByIdAndTenantId(dashboardId, getTenantId())
                .orElseThrow(() -> new RuntimeException("Dashboard not found"));
                
        DashboardWidget widget = widgetRepository.findById(widgetId)
                .orElseThrow(() -> new RuntimeException("Widget not found"));
                
        if (!widget.getDashboardId().equals(dashboardId)) {
            throw new RuntimeException("Widget does not belong to this dashboard");
        }
        
        widgetRepository.deleteById(widgetId);
        cacheService.evict(dashboardId);
    }

    @Transactional
    public DashboardResponse updateLayout(UUID dashboardId, UpdateLayoutRequest request) {
        Dashboard dashboard = dashboardRepository.findByIdAndTenantId(dashboardId, getTenantId())
                .orElseThrow(() -> new RuntimeException("Dashboard not found"));
                
        List<DashboardWidget> widgets = widgetRepository.findByDashboardId(dashboardId);
        
        for (WidgetLayoutDto layout : request.getLayout()) {
            widgets.stream()
                .filter(w -> w.getId().equals(layout.getWidgetId()))
                .findFirst()
                .ifPresent(w -> {
                    w.setX(layout.getX());
                    w.setY(layout.getY());
                    w.setWidth(layout.getWidth());
                    w.setHeight(layout.getHeight());
                });
        }
        
        widgetRepository.saveAll(widgets);
        cacheService.evict(dashboardId);
        
        return toResponse(dashboard, widgets);
    }

    private DashboardResponse toResponse(Dashboard dashboard, List<DashboardWidget> widgets) {
        List<WidgetResponse> widgetResponses = widgets.stream().map(w -> WidgetResponse.builder()
            .id(w.getId())
            .dashboardId(w.getDashboardId())
            .widgetType(w.getWidgetType())
            .title(w.getTitle())
            .x(w.getX())
            .y(w.getY())
            .width(w.getWidth())
            .height(w.getHeight())
            .configJson(w.getConfigJson())
            .build()).collect(Collectors.toList());
            
        return DashboardResponse.builder()
            .id(dashboard.getId())
            .tenantId(dashboard.getTenantId())
            .name(dashboard.getName())
            .description(dashboard.getDescription())
            .publicToken(dashboard.getPublicToken())
            .fullscreenEnabled(dashboard.getFullscreenEnabled())
            .autoRotate(dashboard.getAutoRotate())
            .autoRotateInterval(dashboard.getAutoRotateInterval())
            .createdBy(dashboard.getCreatedBy())
            .layoutJson(dashboard.getLayoutJson())
            .widgets(widgetResponses)
            .build();
    }
    
    private UUID getTenantId() {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) throw new RuntimeException("TenantContext missing");
        return tenantId;
    }
    
    private UUID getUserId() {
        return TenantContext.getUserId();
    }
}
