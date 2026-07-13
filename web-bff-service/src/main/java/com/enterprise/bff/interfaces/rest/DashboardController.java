package com.enterprise.bff.interfaces.rest;

import com.enterprise.bff.application.dto.DashboardViewDTO;
import com.enterprise.bff.application.service.DashboardAggregatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardAggregatorService aggregatorService;

    @GetMapping
    public ResponseEntity<DashboardViewDTO> getDashboard(
            @RequestParam("userId") UUID userId,
            @RequestParam("orgId") UUID orgId) {
            
        DashboardViewDTO dashboard = aggregatorService.getDashboardData(userId, orgId);
        return ResponseEntity.ok(dashboard);
    }
}
