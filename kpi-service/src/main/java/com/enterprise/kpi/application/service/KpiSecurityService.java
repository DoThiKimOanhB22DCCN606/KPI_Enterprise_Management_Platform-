package com.enterprise.kpi.application.service;

import com.enterprise.kpi.infrastructure.persistence.KpiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("kpiSecurity")
@RequiredArgsConstructor
public class KpiSecurityService {
    private final KpiRepository kpiRepo;
    
    public boolean isNotOwner(UUID kpiId, Authentication auth) {
        UUID currentUserId = (UUID) auth.getPrincipal();
        return kpiRepo.findById(kpiId)
            .map(kpi -> !kpi.getOwnerId().equals(currentUserId))
            .orElse(true);
    }
    
    public boolean canEditDraft(UUID kpiId, Authentication auth) {
        UUID currentUserId = (UUID) auth.getPrincipal();
        return kpiRepo.findById(kpiId)
            .map(kpi -> kpi.getOwnerId().equals(currentUserId) && "DRAFT".equals(kpi.getStatus().name()))
            .orElse(false);
    }
}
