package com.enterprise.bff.infrastructure.client;

import com.enterprise.bff.application.dto.KpiSummaryDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "kpi-service", url = "${downstream.kpi-service.url}")
public interface KpiClient {

    @GetMapping("/v1/kpis")
    List<KpiSummaryDTO> getKpisByOwner(@RequestParam("ownerId") UUID ownerId);
}
