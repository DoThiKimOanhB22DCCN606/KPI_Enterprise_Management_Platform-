package com.enterprise.kpi.interfaces.rest;

import com.enterprise.kpi.application.dto.CreateEvaluationCycleRequest;
import com.enterprise.kpi.application.dto.EvaluationCycleDTO;
import com.enterprise.kpi.application.dto.UpdateEvaluationCycleRequest;
import com.enterprise.kpi.application.service.EvaluationCycleService;
import com.enterprise.kpi.infrastructure.config.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/cycles")
@RequiredArgsConstructor
public class EvaluationCycleController {

    private final EvaluationCycleService cycleService;

    @GetMapping
    public ResponseEntity<List<EvaluationCycleDTO>> listCycles() {
        UUID tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(cycleService.listCycles(tenantId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EvaluationCycleDTO> getCycle(@PathVariable UUID id) {
        UUID tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(cycleService.getCycle(tenantId, id));
    }

    @PostMapping
    public ResponseEntity<EvaluationCycleDTO> createCycle(@RequestBody CreateEvaluationCycleRequest request) {
        UUID tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(cycleService.createCycle(tenantId, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EvaluationCycleDTO> updateCycle(
            @PathVariable UUID id, 
            @RequestBody UpdateEvaluationCycleRequest request) {
        UUID tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(cycleService.updateCycle(tenantId, id, request));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<EvaluationCycleDTO> activateCycle(@PathVariable UUID id) {
        UUID tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(cycleService.activateCycle(tenantId, id));
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<EvaluationCycleDTO> closeCycle(@PathVariable UUID id) {
        UUID tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(cycleService.closeCycle(tenantId, id));
    }
}
