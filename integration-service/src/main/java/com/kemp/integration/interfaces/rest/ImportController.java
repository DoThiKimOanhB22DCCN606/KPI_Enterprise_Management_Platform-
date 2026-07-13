package com.kemp.integration.interfaces.rest;

import com.kemp.integration.application.dto.ImportJobResponse;
import com.kemp.integration.application.service.ApiTokenService;
import com.kemp.integration.application.service.ImportService;
import com.kemp.integration.infrastructure.config.TenantContext;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/v1/integrations")
@RequiredArgsConstructor
public class ImportController {

    private final ImportService importService;
    private final ApiTokenService apiTokenService;

    @ModelAttribute
    public void setMockTenantContext(@RequestHeader(value = "X-Tenant-Id", required = false) String tenantId) {
        if (tenantId != null) {
            TenantContext.setTenantId(UUID.fromString(tenantId));
        } else {
            TenantContext.setTenantId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        }
    }

    @PostMapping("/import/csv")
    public ResponseEntity<Map<String, UUID>> uploadCsv(
            @RequestHeader(value = "X-Api-Token", required = false) String apiToken,
            @RequestParam("file") MultipartFile file) throws Exception {
        if (!apiTokenService.validateToken(apiToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UUID jobId = importService.uploadFile(file, "CSV");
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of("jobId", jobId));
    }

    @PostMapping("/import/excel")
    public ResponseEntity<Map<String, UUID>> uploadExcel(
            @RequestHeader(value = "X-Api-Token", required = false) String apiToken,
            @RequestParam("file") MultipartFile file) throws Exception {
        if (!apiTokenService.validateToken(apiToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UUID jobId = importService.uploadFile(file, "EXCEL");
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of("jobId", jobId));
    }

    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<ImportJobResponse> getJobStatus(
            @RequestHeader(value = "X-Api-Token", required = false) String apiToken,
            @PathVariable UUID jobId) {
        if (!apiTokenService.validateToken(apiToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(importService.getJobStatus(jobId));
    }
}
