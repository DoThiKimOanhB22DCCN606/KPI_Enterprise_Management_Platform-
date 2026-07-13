package com.kemp.dashboard.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface WidgetJpaRepository extends JpaRepository<DashboardWidgetEntity, UUID> {
    List<DashboardWidgetEntity> findByDashboardId(UUID dashboardId);
    void deleteByDashboardId(UUID dashboardId);
}
