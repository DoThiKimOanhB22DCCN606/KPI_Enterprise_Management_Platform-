package com.enterprise.analytics.application.service;

import com.enterprise.analytics.application.dto.TrendDataPoint;
import com.enterprise.analytics.infrastructure.persistence.TrendAnalysisRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TrendAggregationServiceTest {

    @Mock
    private TrendAnalysisRepository repository;

    @InjectMocks
    private TrendAggregationService service;

    @Test
    void getKpiTrend_Success() {
        UUID kpiId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        List<TrendDataPoint> expected = List.of(new TrendDataPoint(new Date(), 10.0, 15.0, 5.0, 3L));
        
        when(repository.getKpiTrend(kpiId, tenantId)).thenReturn(expected);

        List<TrendDataPoint> result = service.getKpiTrend(kpiId, tenantId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(10.0, result.get(0).getAvgValue());
    }
}
