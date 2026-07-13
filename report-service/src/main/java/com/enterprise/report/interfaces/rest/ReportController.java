package com.enterprise.report.interfaces.rest;

import com.enterprise.report.application.dto.GenerateReportRequestDTO;
import com.enterprise.report.application.service.AsyncReportService;
import com.enterprise.report.domain.model.ReportRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import com.enterprise.report.domain.model.ReportFormat;
import java.util.Map;
import com.enterprise.report.domain.port.ReportGeneratorPort;
import com.enterprise.report.infrastructure.config.TenantContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final AsyncReportService asyncReportService;
    private final List<ReportGeneratorPort> generators;

    @PostMapping("/generate")
    public ResponseEntity<Map<String, String>> generateReport(@Valid @RequestBody GenerateReportRequestDTO requestDTO) {
        
        String requestId = UUID.randomUUID().toString();
        
        // Map DTO to Domain Request
        ReportRequest domainRequest = ReportRequest.builder()
                .requestId(requestId)
                .tenantId(requestDTO.getTenantId())
                .requestingUserId(requestDTO.getRequestingUserId())
                .format(requestDTO.getFormat())
                .title(requestDTO.getTitle())
                .build();

        asyncReportService.initReport(requestId);
        
        // Dispatch async generation
        asyncReportService.processReport(domainRequest);

        // Return 202 Accepted immediately with requestId
        return ResponseEntity.accepted().body(Map.of("requestId", requestId));
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<AsyncReportService.ReportStatus> getReportStatus(@PathVariable("id") String id) {
        AsyncReportService.ReportStatus status = asyncReportService.getStatus(id);
        if (status.status() == AsyncReportService.Status.NOT_FOUND) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(status);
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportReport(
            @RequestParam(defaultValue = "PDF") String format,
            @RequestParam(defaultValue = "KEMP Report") String title,
            Authentication authentication) {
            
        ReportFormat reportFormat = ReportFormat.valueOf(format.toUpperCase());
        
        UUID tenantId = TenantContext.getTenantId();
        UUID userId = null;
        if (authentication != null && authentication.getPrincipal() instanceof UUID) {
            userId = (UUID) authentication.getPrincipal();
        }
        
        if (tenantId == null) tenantId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        if (userId == null) userId = UUID.fromString("00000000-0000-0000-0000-000000000002");
        
        ReportRequest domainRequest = ReportRequest.builder()
                .requestId(UUID.randomUUID().toString())
                .tenantId(tenantId)
                .requestingUserId(userId)
                .format(reportFormat)
                .title(title)
                .build();
                
        ReportGeneratorPort generator = generators.stream()
                .filter(g -> g.supports(reportFormat))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported format: " + format));
                
        byte[] content = generator.generateReport(domainRequest);
        
        HttpHeaders headers = new HttpHeaders();
        if (reportFormat == ReportFormat.PDF) {
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "kpi-report.pdf");
        } else {
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", "kpi-report.xlsx");
        }
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(content);
    }
}
