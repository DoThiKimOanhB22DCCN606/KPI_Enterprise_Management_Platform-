package com.enterprise.analytics.application.service;

import com.enterprise.analytics.application.dto.TrendDataDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import com.enterprise.analytics.infrastructure.persistence.TrendAnalysisRepository;
import com.enterprise.analytics.application.dto.TrendDataPoint;
import com.enterprise.analytics.application.dto.VarianceResult;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrendAggregationService {

    private final TrendAnalysisRepository repository;

    public List<TrendDataPoint> getKpiTrend(UUID kpiId, UUID tenantId) {
        return repository.getKpiTrend(kpiId, tenantId);
    }

    public VarianceResult getVariance(UUID kpiId, UUID tenantId, LocalDate currentPeriodStart, LocalDate previousPeriodStart) {
        return repository.getVariance(kpiId, tenantId, currentPeriodStart, previousPeriodStart);
    }

    public java.util.Map<String, Object> getKpiPerformance(UUID kpiId, UUID tenantId) {
        return repository.getKpiPerformance(kpiId, tenantId);
    }
}
