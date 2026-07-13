package com.enterprise.kpi.interfaces.rest;

import com.enterprise.kpi.application.dto.KpiDTO;
import com.enterprise.kpi.application.dto.KpiTemplateCreateRequest;
import com.enterprise.kpi.application.dto.KpiTemplateDTO;
import com.enterprise.kpi.application.dto.KpiTemplateUpdateRequest;
import com.enterprise.kpi.application.service.KpiTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/kpi-templates")
@RequiredArgsConstructor
public class KpiTemplateController {

    private final KpiTemplateService templateService;

    @PreAuthorize("hasAnyRole('TENANT_ADMIN','HR_ADMIN','STORE_MANAGER','EMPLOYEE')")
    @GetMapping
    public ResponseEntity<List<KpiTemplateDTO>> listTemplates(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(templateService.listTemplates(category));
    }

    @PreAuthorize("hasAnyRole('TENANT_ADMIN','HR_ADMIN')")
    @PostMapping
    public ResponseEntity<KpiTemplateDTO> createTemplate(@Valid @RequestBody KpiTemplateCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(templateService.createTemplate(request));
    }

    @PreAuthorize("hasAnyRole('TENANT_ADMIN','HR_ADMIN','STORE_MANAGER','EMPLOYEE')")
    @GetMapping("/{id}")
    public ResponseEntity<KpiTemplateDTO> getTemplate(@PathVariable UUID id) {
        return ResponseEntity.ok(templateService.getTemplate(id));
    }

    @PreAuthorize("hasAnyRole('TENANT_ADMIN','HR_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<KpiTemplateDTO> updateTemplate(
            @PathVariable UUID id, 
            @Valid @RequestBody KpiTemplateUpdateRequest request) {
        return ResponseEntity.ok(templateService.updateTemplate(id, request));
    }

    @PreAuthorize("hasAnyRole('TENANT_ADMIN','HR_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable UUID id) {
        templateService.deleteTemplate(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('TENANT_ADMIN','HR_ADMIN')")
    @PostMapping("/{id}/clone")
    public ResponseEntity<KpiTemplateDTO> cloneTemplate(@PathVariable UUID id) {
        return ResponseEntity.ok(templateService.cloneTemplate(id));
    }

    @PreAuthorize("hasAnyRole('TENANT_ADMIN','HR_ADMIN')")
    @GetMapping("/{id}/export")
    public ResponseEntity<KpiTemplateDTO> exportTemplate(@PathVariable UUID id) {
        // Simple export just returns the template DTO for now
        return ResponseEntity.ok(templateService.getTemplate(id));
    }

    @PreAuthorize("hasAnyRole('TENANT_ADMIN','HR_ADMIN')")
    @PostMapping("/import")
    public ResponseEntity<KpiTemplateDTO> importTemplate(@Valid @RequestBody KpiTemplateCreateRequest request) {
        // Simple import creates a new template
        return ResponseEntity.status(HttpStatus.CREATED).body(templateService.createTemplate(request));
    }

    @PreAuthorize("hasAnyRole('TENANT_ADMIN','HR_ADMIN')")
    @PostMapping("/{id}/apply")
    public ResponseEntity<KpiDTO> applyTemplate(
            @PathVariable UUID id,
            @RequestParam UUID ownerId) {
        return ResponseEntity.ok(templateService.applyTemplate(id, ownerId));
    }
}
