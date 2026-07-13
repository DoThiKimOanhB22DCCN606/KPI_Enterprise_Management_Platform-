package com.enterprise.report.infrastructure.adapter.client;

import com.enterprise.report.application.dto.KpiDTO;
import com.enterprise.report.application.dto.PaginatedResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "kpi-service", url = "${kpi.service.url:http://kpi-service:8082}")
public interface KpiClient {

    @GetMapping("/v1/kpis")
    PaginatedResponse<KpiDTO> getKpis(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Roles") String roles,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "100") int size
    );
}
