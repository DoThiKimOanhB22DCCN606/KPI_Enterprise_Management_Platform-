package com.enterprise.kpi.application.service;

import com.enterprise.kpi.application.dto.*;
import com.enterprise.kpi.domain.exception.BusinessRuleException;
import com.enterprise.kpi.domain.model.KpiStatus;
import com.enterprise.kpi.infrastructure.persistence.KpiEntity;
import com.enterprise.kpi.infrastructure.persistence.KpiRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class KpiServiceTest {

    @Mock
    private KpiRepository kpiRepository;

    @Mock
    private FormulaEvaluatorService evaluatorService;

    @InjectMocks
    private KpiService kpiService;

    private UUID kpiId;
    private UUID tenantId;

    @BeforeEach
    void setUp() {
        kpiId = UUID.randomUUID();
        tenantId = UUID.randomUUID();
        com.enterprise.kpi.infrastructure.config.TenantContext.setTenantId(tenantId);
    }

    private KpiEntity createMockEntity(KpiStatus status) {
        KpiEntity entity = new KpiEntity();
        entity.setId(kpiId);
        entity.setTenantId(tenantId);
        entity.setStatus(status);
        entity.setTarget(new BigDecimal("100.0"));
        entity.setCurrentProgress(new BigDecimal("0.0"));
        return entity;
    }

    @Test
    void createKpi_Success() {
        KpiCreateRequest request = new KpiCreateRequest();
        request.setTarget(new BigDecimal("100"));
        request.setFormula("a + b");

        when(kpiRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        KpiDTO result = kpiService.createKpi(request);

        assertNotNull(result);
        assertEquals(KpiStatus.DRAFT, result.getStatus());
        assertEquals("a + b", result.getFormula());
    }

    @Test
    void recordKpiProgress_Success() {
        KpiEntity entity = createMockEntity(KpiStatus.ACTIVE);
        when(kpiRepository.findByIdAndTenantId(kpiId, tenantId)).thenReturn(Optional.of(entity));
        when(kpiRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        KpiProgressRecordRequest request = new KpiProgressRecordRequest();
        request.setValue(new BigDecimal("50.0"));

        KpiDTO result = kpiService.recordKpiProgress(kpiId, request);

        assertEquals(new BigDecimal("50.0"), result.getCurrentProgress());
    }

    @Test
    void recordKpiProgress_ClosedKpi_ThrowsException() {
        KpiEntity entity = createMockEntity(KpiStatus.CLOSED);
        when(kpiRepository.findByIdAndTenantId(kpiId, tenantId)).thenReturn(Optional.of(entity));

        KpiProgressRecordRequest request = new KpiProgressRecordRequest();
        request.setValue(new BigDecimal("50.0"));

        assertThrows(BusinessRuleException.class, () -> kpiService.recordKpiProgress(kpiId, request));
    }

    @Test
    void updateFormula_Success() {
        KpiEntity entity = createMockEntity(KpiStatus.DRAFT);
        when(kpiRepository.findByIdAndTenantId(kpiId, tenantId)).thenReturn(Optional.of(entity));
        when(kpiRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        KpiFormulaUpdateRequest request = new KpiFormulaUpdateRequest();
        request.setFormula("x * y");

        KpiDTO result = kpiService.updateFormula(kpiId, request);

        assertEquals("x * y", result.getFormula());
    }

    @Test
    void calculateKpi_Success() {
        KpiEntity entity = createMockEntity(KpiStatus.ACTIVE);
        entity.setFormula("val1 + val2");
        when(kpiRepository.findByIdAndTenantId(kpiId, tenantId)).thenReturn(Optional.of(entity));
        when(evaluatorService.evaluate(eq("val1 + val2"), any())).thenReturn(75.0);
        when(kpiRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        KpiCalculateRequest request = new KpiCalculateRequest();
        request.setVariables(Map.of("val1", 50.0, "val2", 25.0));

        KpiDTO result = kpiService.calculateKpi(kpiId, request);

        assertEquals(new BigDecimal("75.0"), result.getCurrentProgress());
    }

    @Test
    void getKpiById_Success() {
        KpiEntity entity = createMockEntity(KpiStatus.ACTIVE);
        when(kpiRepository.findByIdAndTenantId(kpiId, tenantId)).thenReturn(Optional.of(entity));

        KpiDTO result = kpiService.getKpiById(kpiId);

        assertNotNull(result);
        assertEquals(kpiId, result.getId());
        assertEquals(KpiStatus.ACTIVE, result.getStatus());
    }

    @Test
    void getKpiById_NotFound_ThrowsException() {
        when(kpiRepository.findByIdAndTenantId(kpiId, tenantId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> kpiService.getKpiById(kpiId));
    }
}
