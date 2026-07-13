package com.kemp.dashboard.infrastructure.persistence;

import com.kemp.dashboard.domain.model.DashboardWidget;
import com.kemp.dashboard.domain.repository.WidgetRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WidgetRepositoryAdapter implements WidgetRepository {
    
    private final WidgetJpaRepository repository;

    @Override
    public DashboardWidget save(DashboardWidget widget) {
        DashboardWidgetEntity entity = toEntity(widget);
        return toDomain(repository.save(entity));
    }

    @Override
    public List<DashboardWidget> saveAll(List<DashboardWidget> widgets) {
        List<DashboardWidgetEntity> entities = widgets.stream().map(this::toEntity).collect(Collectors.toList());
        return repository.saveAll(entities).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<DashboardWidget> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public List<DashboardWidget> findByDashboardId(UUID dashboardId) {
        return repository.findByDashboardId(dashboardId).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public void deleteByDashboardId(UUID dashboardId) {
        repository.deleteByDashboardId(dashboardId);
    }
    
    private DashboardWidgetEntity toEntity(DashboardWidget domain) {
        DashboardWidgetEntity entity = new DashboardWidgetEntity();
        entity.setId(domain.getId() == null ? UUID.randomUUID() : domain.getId());
        entity.setDashboardId(domain.getDashboardId());
        entity.setWidgetType(domain.getWidgetType());
        entity.setTitle(domain.getTitle());
        entity.setX(domain.getX());
        entity.setY(domain.getY());
        entity.setWidth(domain.getWidth());
        entity.setHeight(domain.getHeight());
        entity.setConfigJson(domain.getConfigJson());
        return entity;
    }
    
    private DashboardWidget toDomain(DashboardWidgetEntity entity) {
        return DashboardWidget.builder()
            .id(entity.getId())
            .dashboardId(entity.getDashboardId())
            .widgetType(entity.getWidgetType())
            .title(entity.getTitle())
            .x(entity.getX())
            .y(entity.getY())
            .width(entity.getWidth())
            .height(entity.getHeight())
            .configJson(entity.getConfigJson())
            .build();
    }
}
