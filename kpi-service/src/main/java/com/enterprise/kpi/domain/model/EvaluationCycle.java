package com.enterprise.kpi.domain.model;

import com.enterprise.kpi.domain.exception.InvalidStateException;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
public class EvaluationCycle {
    private final UUID id;
    private final UUID tenantId;
    private String name;
    private CycleType type;
    private OffsetDateTime periodStart;
    private OffsetDateTime periodEnd;
    private CycleStatus status;
    private int kpiCount;

    public EvaluationCycle(UUID id, UUID tenantId, String name, CycleType type, 
                           OffsetDateTime periodStart, OffsetDateTime periodEnd, 
                           CycleStatus status, int kpiCount) {
        this.id = id;
        this.tenantId = tenantId;
        this.name = name;
        this.type = type;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.status = status;
        this.kpiCount = kpiCount;
    }

    public static EvaluationCycle create(UUID id, UUID tenantId, String name, CycleType type, 
                                         OffsetDateTime periodStart, OffsetDateTime periodEnd) {
        if (periodEnd.isBefore(periodStart) || periodEnd.isEqual(periodStart)) {
            throw new IllegalArgumentException("ERR_CYCLE_DATE_RANGE_INVALID: periodEnd must be strictly after periodStart");
        }
        return new EvaluationCycle(id, tenantId, name, type, periodStart, periodEnd, CycleStatus.DRAFT, 0);
    }

    public void open() {
        if (this.status != CycleStatus.DRAFT) {
            throw new InvalidStateException("ERR_CYCLE_STATE_INVALID: Can only open a DRAFT cycle");
        }
        this.status = CycleStatus.OPEN;
    }

    public void lock() {
        if (this.status != CycleStatus.OPEN) {
            throw new InvalidStateException("ERR_CYCLE_STATE_INVALID: Can only lock an OPEN cycle");
        }
        this.status = CycleStatus.LOCKED;
    }

    public void close() {
        if (this.status != CycleStatus.LOCKED) {
            throw new InvalidStateException("ERR_CYCLE_STATE_INVALID: Can only close a LOCKED cycle");
        }
        this.status = CycleStatus.CLOSED;
    }

    public void archive() {
        if (this.status != CycleStatus.CLOSED) {
            throw new InvalidStateException("ERR_CYCLE_STATE_INVALID: Can only archive a CLOSED cycle");
        }
        this.status = CycleStatus.ARCHIVED;
    }

    public void updateDetails(String name, OffsetDateTime periodStart, OffsetDateTime periodEnd) {
        if (this.status != CycleStatus.DRAFT && this.status != CycleStatus.OPEN) {
            throw new InvalidStateException("ERR_CYCLE_STATE_INVALID: Can only edit a DRAFT or OPEN cycle");
        }
        if (periodEnd.isBefore(periodStart) || periodEnd.isEqual(periodStart)) {
            throw new IllegalArgumentException("ERR_CYCLE_DATE_RANGE_INVALID: periodEnd must be strictly after periodStart");
        }
        this.name = name;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
    }
}
