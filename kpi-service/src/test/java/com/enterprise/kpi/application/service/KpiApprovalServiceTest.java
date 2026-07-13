package com.enterprise.kpi.application.service;

import com.enterprise.kpi.application.dto.KpiApprovalRequest;
import com.enterprise.kpi.application.dto.KpiDTO;
import com.enterprise.kpi.application.service.port.OrganizationClientPort;
import com.enterprise.kpi.domain.exception.BusinessRuleException;
import com.enterprise.kpi.domain.model.KpiStatus;
import com.enterprise.kpi.domain.model.ApprovalAction;
import com.enterprise.kpi.infrastructure.persistence.KpiEntity;
import com.enterprise.kpi.infrastructure.persistence.KpiRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class KpiApprovalServiceTest {

    @Mock
    private KpiRepository kpiRepository;

    @Mock
    private FormulaEvaluatorService evaluatorService;

    @Mock
    private OrganizationClientPort organizationClientPort;

    @InjectMocks
    private KpiService kpiService;

    private UUID kpiId;
    private UUID tenantId;
    private UUID ownerId;
    private UUID managerId;
    private UUID directorId;

    @BeforeEach
    void setUp() {
        kpiId = UUID.randomUUID();
        tenantId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        ownerId = UUID.randomUUID();
        managerId = UUID.randomUUID();
        directorId = UUID.randomUUID();
        com.enterprise.kpi.infrastructure.config.TenantContext.setTenantId(tenantId);
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        com.enterprise.kpi.infrastructure.config.TenantContext.clear();
    }

    private KpiEntity createMockEntity(KpiStatus status) {
        KpiEntity entity = new KpiEntity();
        entity.setId(kpiId);
        entity.setTenantId(tenantId);
        entity.setOwnerId(ownerId);
        entity.setStatus(status);
        return entity;
    }

    @Test
    void submitForApproval_Success() {
        KpiEntity draftEntity = createMockEntity(KpiStatus.DRAFT);
        when(kpiRepository.findByIdAndTenantId(kpiId, tenantId)).thenReturn(Optional.of(draftEntity));
        when(kpiRepository.save(any(KpiEntity.class))).thenAnswer(i -> i.getArguments()[0]);

        KpiApprovalRequest request = new KpiApprovalRequest();
        request.setAction(ApprovalAction.SUBMIT);

        KpiDTO result = kpiService.processApproval(kpiId, request, ownerId);

        assertEquals(KpiStatus.PENDING_MANAGER, result.getStatus());
        verify(kpiRepository).save(any(KpiEntity.class));
    }

    @Test
    void submitForApproval_ByNonOwner_ThrowsException() {
        KpiEntity draftEntity = createMockEntity(KpiStatus.DRAFT);
        when(kpiRepository.findByIdAndTenantId(kpiId, tenantId)).thenReturn(Optional.of(draftEntity));

        KpiApprovalRequest request = new KpiApprovalRequest();
        request.setAction(ApprovalAction.SUBMIT);

        assertThrows(BusinessRuleException.class, () -> 
            kpiService.processApproval(kpiId, request, UUID.randomUUID())
        );
    }

    @Test
    void approveByManager_Success() {
        KpiEntity pendingManagerEntity = createMockEntity(KpiStatus.PENDING_MANAGER);
        when(kpiRepository.findByIdAndTenantId(kpiId, tenantId)).thenReturn(Optional.of(pendingManagerEntity));
        when(organizationClientPort.isDirectManager(managerId, ownerId)).thenReturn(true);
        when(kpiRepository.save(any(KpiEntity.class))).thenAnswer(i -> i.getArguments()[0]);

        KpiApprovalRequest request = new KpiApprovalRequest();
        request.setAction(ApprovalAction.APPROVE);

        KpiDTO result = kpiService.processApproval(kpiId, request, managerId);

        assertEquals(KpiStatus.PENDING_DIRECTOR, result.getStatus());
    }

    @Test
    void approveByManager_ByNonManager_ThrowsException() {
        KpiEntity pendingManagerEntity = createMockEntity(KpiStatus.PENDING_MANAGER);
        when(kpiRepository.findByIdAndTenantId(kpiId, tenantId)).thenReturn(Optional.of(pendingManagerEntity));
        when(organizationClientPort.isDirectManager(managerId, ownerId)).thenReturn(false);

        KpiApprovalRequest request = new KpiApprovalRequest();
        request.setAction(ApprovalAction.APPROVE);

        assertThrows(BusinessRuleException.class, () -> 
            kpiService.processApproval(kpiId, request, managerId)
        );
    }

    @Test
    void approveByDirector_Success() {
        KpiEntity pendingDirectorEntity = createMockEntity(KpiStatus.PENDING_DIRECTOR);
        when(kpiRepository.findByIdAndTenantId(kpiId, tenantId)).thenReturn(Optional.of(pendingDirectorEntity));
        when(organizationClientPort.isDirector(directorId, ownerId)).thenReturn(true);
        when(kpiRepository.save(any(KpiEntity.class))).thenAnswer(i -> i.getArguments()[0]);

        KpiApprovalRequest request = new KpiApprovalRequest();
        request.setAction(ApprovalAction.APPROVE);

        KpiDTO result = kpiService.processApproval(kpiId, request, directorId);

        assertEquals(KpiStatus.APPROVED, result.getStatus());
    }
}
