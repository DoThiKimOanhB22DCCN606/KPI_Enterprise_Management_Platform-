package com.enterprise.kpi.infrastructure.scheduler;

import com.enterprise.kpi.domain.model.KpiStatus;
import com.enterprise.kpi.infrastructure.persistence.KpiEntity;
import com.enterprise.kpi.infrastructure.persistence.KpiRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class KpiLifecycleScheduler {

    private final KpiRepository kpiRepository;

    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void activateApprovedKpis() {
        List<KpiEntity> kpis = kpiRepository.findByStatusAndStartDateLessThanEqual(KpiStatus.APPROVED, LocalDate.now());
        for (KpiEntity kpi : kpis) {
            kpi.setStatus(KpiStatus.ACTIVE);
            kpiRepository.save(kpi);
            log.info("Activated KPI {}", kpi.getId());
        }
    }

    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void completeActiveKpis() {
        List<KpiEntity> kpis = kpiRepository.findByStatusAndEndDateBefore(KpiStatus.ACTIVE, LocalDate.now());
        for (KpiEntity kpi : kpis) {
            kpi.setStatus(KpiStatus.COMPLETED);
            kpiRepository.save(kpi);
            log.info("Completed KPI {}", kpi.getId());
        }
    }

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void archiveClosedKpis() {
        Instant cutoff = Instant.now().minus(3, ChronoUnit.YEARS);
        List<KpiEntity> kpis = kpiRepository.findByStatusAndUpdatedAtBefore(KpiStatus.CLOSED, cutoff);
        for (KpiEntity kpi : kpis) {
            kpi.setStatus(KpiStatus.ARCHIVED);
            kpiRepository.save(kpi);
            log.info("Archived KPI {}", kpi.getId());
        }
    }
}
