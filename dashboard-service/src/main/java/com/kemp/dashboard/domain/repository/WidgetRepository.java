package com.kemp.dashboard.domain.repository;

import com.kemp.dashboard.domain.model.DashboardWidget;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WidgetRepository {
    DashboardWidget save(DashboardWidget widget);
    List<DashboardWidget> saveAll(List<DashboardWidget> widgets);
    Optional<DashboardWidget> findById(UUID id);
    List<DashboardWidget> findByDashboardId(UUID dashboardId);
    void deleteById(UUID id);
    void deleteByDashboardId(UUID dashboardId);
}
