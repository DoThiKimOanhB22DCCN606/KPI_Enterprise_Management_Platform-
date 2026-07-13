package com.enterprise.kpi.application.service;

import com.enterprise.kpi.application.dto.CreateEvaluationCycleRequest;
import com.enterprise.kpi.application.dto.EvaluationCycleDTO;
import com.enterprise.kpi.application.dto.UpdateEvaluationCycleRequest;
import com.enterprise.kpi.domain.model.CycleStatus;
import com.enterprise.kpi.domain.model.CycleType;
import com.enterprise.kpi.infrastructure.messaging.KpiEventPublisher;
import com.enterprise.kpi.infrastructure.messaging.event.AuditEventDTO;
import com.enterprise.kpi.infrastructure.persistence.EvaluationCycleEntity;
import com.enterprise.kpi.infrastructure.persistence.EvaluationCycleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EvaluationCycleServiceTest {

    @Mock
    private EvaluationCycleRepository repository;

    @Mock
    private KpiEventPublisher eventPublisher;

    @InjectMocks
    private EvaluationCycleService service;

    private UUID tenantId;
    private UUID cycleId;
    private EvaluationCycleEntity draftEntity;
    private EvaluationCycleEntity activeEntity;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        cycleId = UUID.randomUUID();

        draftEntity = new EvaluationCycleEntity();
        draftEntity.setId(cycleId);
        draftEntity.setTenantId(tenantId);
        draftEntity.setName("Draft Cycle");
        draftEntity.setType(CycleType.QUARTERLY);
        draftEntity.setPeriodStart(OffsetDateTime.now().plusDays(1));
        draftEntity.setPeriodEnd(OffsetDateTime.now().plusDays(90));
        draftEntity.setStatus(CycleStatus.DRAFT);
        draftEntity.setKpiCount(0);

        activeEntity = new EvaluationCycleEntity();
        activeEntity.setId(cycleId);
        activeEntity.setTenantId(tenantId);
        activeEntity.setName("Active Cycle");
        activeEntity.setType(CycleType.QUARTERLY);
        activeEntity.setPeriodStart(OffsetDateTime.now().plusDays(1));
        activeEntity.setPeriodEnd(OffsetDateTime.now().plusDays(90));
        activeEntity.setStatus(CycleStatus.OPEN);
        activeEntity.setKpiCount(5);
    }

    @Test
    void createCycle_Success() {
        CreateEvaluationCycleRequest request = new CreateEvaluationCycleRequest();
        request.setName("New Cycle");
        request.setType(CycleType.YEARLY);
        request.setPeriodStart(OffsetDateTime.now().plusDays(1));
        request.setPeriodEnd(OffsetDateTime.now().plusDays(365));

        when(repository.save(any(EvaluationCycleEntity.class))).thenAnswer(i -> i.getArguments()[0]);

        EvaluationCycleDTO result = service.createCycle(tenantId, request);

        assertNotNull(result);
        assertEquals("New Cycle", result.getName());
        assertEquals(CycleStatus.DRAFT, result.getStatus());
        verify(repository).save(any(EvaluationCycleEntity.class));
    }

    @Test
    void listCycles_Success() {
        when(repository.findAllByTenantId(tenantId)).thenReturn(List.of(draftEntity, activeEntity));

        List<EvaluationCycleDTO> result = service.listCycles(tenantId);

        assertEquals(2, result.size());
        verify(repository).findAllByTenantId(tenantId);
    }

    @Test
    void getCycle_Success() {
        when(repository.findByIdAndTenantId(cycleId, tenantId)).thenReturn(Optional.of(draftEntity));

        EvaluationCycleDTO result = service.getCycle(tenantId, cycleId);

        assertNotNull(result);
        assertEquals(draftEntity.getName(), result.getName());
    }

    @Test
    void updateCycle_Draft_Success() {
        when(repository.findByIdAndTenantId(cycleId, tenantId)).thenReturn(Optional.of(draftEntity));
        when(repository.save(any(EvaluationCycleEntity.class))).thenAnswer(i -> i.getArguments()[0]);

        UpdateEvaluationCycleRequest request = new UpdateEvaluationCycleRequest();
        request.setName("Updated Draft");
        request.setPeriodStart(draftEntity.getPeriodStart());
        request.setPeriodEnd(draftEntity.getPeriodEnd().plusDays(10));

        EvaluationCycleDTO result = service.updateCycle(tenantId, cycleId, request);

        assertEquals("Updated Draft", result.getName());
        verify(repository).save(any(EvaluationCycleEntity.class));
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void updateCycle_Active_MissingReason_ThrowsException() {
        when(repository.findByIdAndTenantId(cycleId, tenantId)).thenReturn(Optional.of(activeEntity));

        UpdateEvaluationCycleRequest request = new UpdateEvaluationCycleRequest();
        request.setName("Updated Active");
        request.setPeriodStart(activeEntity.getPeriodStart());
        request.setPeriodEnd(activeEntity.getPeriodEnd().plusDays(10));
        // Reason is null

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            service.updateCycle(tenantId, cycleId, request);
        });

        assertTrue(ex.getMessage().contains("Reason is mandatory"));
        verify(repository, never()).save(any());
    }

    @Test
    void updateCycle_Active_WithReason_Success() {
        when(repository.findByIdAndTenantId(cycleId, tenantId)).thenReturn(Optional.of(activeEntity));
        when(repository.save(any(EvaluationCycleEntity.class))).thenAnswer(i -> i.getArguments()[0]);

        UpdateEvaluationCycleRequest request = new UpdateEvaluationCycleRequest();
        request.setName("Updated Active");
        request.setPeriodStart(activeEntity.getPeriodStart());
        request.setPeriodEnd(activeEntity.getPeriodEnd().plusDays(10));
        request.setReason("Adjusting timeline due to holiday");

        EvaluationCycleDTO result = service.updateCycle(tenantId, cycleId, request);

        assertEquals("Updated Active", result.getName());
        
        ArgumentCaptor<AuditEventDTO> auditCaptor = ArgumentCaptor.forClass(AuditEventDTO.class);
        verify(eventPublisher).publishAuditEvent(auditCaptor.capture());
        
        AuditEventDTO auditEvent = auditCaptor.getValue();
        assertEquals("CYCLE_UPDATED", auditEvent.getEventType());
        assertNotNull(auditEvent.getChanges().get("reason"));
        assertEquals("Adjusting timeline due to holiday", auditEvent.getChanges().get("reason"));
    }

    @Test
    void activateCycle_Success() {
        when(repository.findByIdAndTenantId(cycleId, tenantId)).thenReturn(Optional.of(draftEntity));
        when(repository.save(any(EvaluationCycleEntity.class))).thenAnswer(i -> i.getArguments()[0]);

        EvaluationCycleDTO result = service.activateCycle(tenantId, cycleId);

        assertEquals(CycleStatus.OPEN, result.getStatus());
        verify(repository).save(any(EvaluationCycleEntity.class));
    }
}
