package com.enterprise.kpi.interfaces.rest;

import com.enterprise.kpi.application.dto.KpiApprovalRequest;
import com.enterprise.kpi.application.dto.KpiCreateRequest;
import com.enterprise.kpi.application.dto.KpiDTO;
import com.enterprise.kpi.application.dto.KpiFormulaUpdateRequest;
import com.enterprise.kpi.application.dto.KpiCalculateRequest;
import com.enterprise.kpi.application.dto.KpiProgressRecordRequest;
import com.enterprise.kpi.application.service.KpiUseCase;
import org.springframework.security.core.Authentication;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.List;

import com.enterprise.kpi.application.dto.AttachmentResponse;
import com.enterprise.kpi.application.service.KpiAttachmentService;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/v1/kpis")
@RequiredArgsConstructor
public class KpiController {

    private final KpiUseCase kpiUseCase;
    private final KpiAttachmentService attachmentService;

    @PreAuthorize("hasAnyRole('TENANT_ADMIN','HR_ADMIN','TEAM_LEADER','STORE_MANAGER','REGIONAL_MANAGER','EMPLOYEE')")
    @PostMapping
    public ResponseEntity<KpiDTO> createKpi(@Valid @RequestBody KpiCreateRequest request) {
        KpiDTO response = kpiUseCase.createKpi(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasAnyRole('TENANT_ADMIN','HR_ADMIN')")
    @PostMapping("/templates")
    public ResponseEntity<Void> createKpiTemplate() {
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE','TEAM_LEADER','STORE_MANAGER','REGIONAL_MANAGER','TENANT_ADMIN')")
    @PostMapping("/{id}/approvals")
    public ResponseEntity<KpiDTO> processApproval(
            @PathVariable UUID id,
            @Valid @RequestBody KpiApprovalRequest request,
            Authentication authentication) {
        UUID currentUserId = (UUID) authentication.getPrincipal();
        KpiDTO response = kpiUseCase.processApproval(id, request, currentUserId);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE','TEAM_LEADER','STORE_MANAGER','REGIONAL_MANAGER','TENANT_ADMIN')")
    @PutMapping("/{id}/progress")
    public ResponseEntity<?> recordKpiProgress(
            @PathVariable UUID id,
            @Valid @RequestBody KpiProgressRecordRequest request) {
        try {
            KpiDTO response = kpiUseCase.recordKpiProgress(id, request);
            return ResponseEntity.ok(response);
        } catch (com.enterprise.kpi.domain.exception.BusinessRuleException ex) {
            if ("ERR_CYCLE_STATE_INVALID".equals(ex.getCode())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(java.util.Map.of("error", "ERR_CYCLE_STATE_INVALID", "message", ex.getMessage()));
            }
            throw ex;
        } catch (com.enterprise.kpi.domain.exception.InvalidStateException ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("ERR_CYCLE_STATE_INVALID")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(java.util.Map.of("error", "ERR_CYCLE_STATE_INVALID", "message", ex.getMessage()));
            }
            throw ex;
        }
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE','TEAM_LEADER','STORE_MANAGER','REGIONAL_MANAGER','TENANT_ADMIN')")
    @PutMapping("/{id}/formula")
    public ResponseEntity<KpiDTO> updateKpiFormula(
            @PathVariable UUID id,
            @Valid @RequestBody KpiFormulaUpdateRequest request) {
        KpiDTO response = kpiUseCase.updateFormula(id, request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE','TEAM_LEADER','STORE_MANAGER','REGIONAL_MANAGER','TENANT_ADMIN')")
    @PostMapping("/{id}/calculate")
    public ResponseEntity<KpiDTO> calculateKpi(
            @PathVariable UUID id,
            @Valid @RequestBody KpiCalculateRequest request) {
        KpiDTO response = kpiUseCase.calculateKpi(id, request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE','TEAM_LEADER','STORE_MANAGER','REGIONAL_MANAGER','TENANT_ADMIN','HR_ADMIN')")
    @GetMapping
    public ResponseEntity<org.springframework.data.domain.Page<KpiDTO>> getKpis(
            org.springframework.data.domain.Pageable pageable,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID ownerId) {
        return ResponseEntity.ok(kpiUseCase.getKpis(pageable, status, ownerId));
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE','TEAM_LEADER','STORE_MANAGER','REGIONAL_MANAGER','TENANT_ADMIN','HR_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<KpiDTO> getKpiById(@PathVariable UUID id) {
        return ResponseEntity.ok(kpiUseCase.getKpiById(id));
    }

    @PostMapping("/{id}/values/{valueId}/attachments")
    public ResponseEntity<AttachmentResponse> uploadAttachment(
            @PathVariable UUID id,
            @PathVariable UUID valueId,
            @RequestParam("file") MultipartFile file) throws Exception {
        return ResponseEntity.ok(attachmentService.uploadAttachment(id, valueId, file));
    }

    @GetMapping("/{id}/values/{valueId}/attachments")
    public ResponseEntity<List<AttachmentResponse>> listAttachments(
            @PathVariable UUID id,
            @PathVariable UUID valueId) {
        return ResponseEntity.ok(attachmentService.listAttachments(valueId));
    }

    @PreAuthorize("hasAnyRole('TEAM_LEADER','STORE_MANAGER','REGIONAL_MANAGER','TENANT_ADMIN')")
    @PostMapping("/{id}/complete")
    public ResponseEntity<KpiDTO> completeKpi(@PathVariable UUID id, @Valid @RequestBody com.enterprise.kpi.application.dto.KpiCompleteRequest request) {
        return ResponseEntity.ok(kpiUseCase.completeKpi(id, request));
    }

    @PreAuthorize("hasAnyRole('TENANT_ADMIN','HR_ADMIN','EMPLOYEE','TEAM_LEADER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteKpi(@PathVariable UUID id) {
        kpiUseCase.deleteKpi(id);
        return ResponseEntity.noContent().build();
    }
}
