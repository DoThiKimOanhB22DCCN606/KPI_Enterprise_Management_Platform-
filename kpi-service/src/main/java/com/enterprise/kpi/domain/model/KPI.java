package com.enterprise.kpi.domain.model;

import com.enterprise.kpi.domain.exception.DomainAuthorizationException;
import com.enterprise.kpi.domain.exception.InvalidStateException;
import com.enterprise.kpi.domain.exception.BusinessRuleException;
import java.math.BigDecimal;
import java.util.UUID;

public class KPI {
    
    private UUID id;
    private UUID tenantId;
    private UUID templateId;
    private UUID ownerId;
    private String name;
    private String frequency;
    private KpiStatus status;
    private BigDecimal target;
    private BigDecimal currentProgress;
    private String formula;
    private BigDecimal evaluationScore;
    private String managerComments;
    private UUID cycleId;

    // Standard constructor for recreating from persistence
    public KPI(UUID id, UUID tenantId, UUID templateId, UUID ownerId, String name, String frequency, KpiStatus status, BigDecimal target, BigDecimal currentProgress, String formula, BigDecimal evaluationScore, String managerComments, UUID cycleId) {
        this.id = id;
        this.tenantId = tenantId;
        this.templateId = templateId;
        this.ownerId = ownerId;
        this.name = name;
        this.frequency = frequency;
        this.status = status;
        this.target = target;
        this.currentProgress = currentProgress;
        this.formula = formula;
        this.evaluationScore = evaluationScore;
        this.managerComments = managerComments;
        this.cycleId = cycleId;
    }

    // Factory method for creating a new KPI
    public static KPI create(UUID id, UUID tenantId, UUID templateId, UUID ownerId, String name, String frequency, BigDecimal target, String formula, UUID cycleId) {
        return new KPI(id, tenantId, templateId, ownerId, name, frequency, KpiStatus.DRAFT, target, BigDecimal.ZERO, formula, null, null, cycleId);
    }

    /**
     * Reject the KPI back to DRAFT.
     */
    public void reject(UUID approverId, String comment) {
        if (this.status != KpiStatus.PENDING_MANAGER && this.status != KpiStatus.PENDING_DIRECTOR) {
            throw new InvalidStateException("KPI must be in a pending state to be rejected.");
        }
        if (this.ownerId.equals(approverId)) {
            throw new DomainAuthorizationException("An owner cannot reject their own KPI.");
        }
        if (comment == null || comment.trim().isEmpty()) {
            throw new IllegalArgumentException("A rejection comment is required.");
        }
        this.status = KpiStatus.DRAFT;
    }

    public void activate() {
        if (this.status != KpiStatus.APPROVED) {
            throw new InvalidStateException("KPI must be in APPROVED state to activate.");
        }
        this.status = KpiStatus.ACTIVE;
    }

    public void complete(BigDecimal evaluationScore, String managerComments) {
        if (this.status != KpiStatus.ACTIVE) {
            throw new InvalidStateException("KPI must be in ACTIVE state to complete.");
        }
        if (evaluationScore == null) {
            throw new IllegalArgumentException("Evaluation score is required.");
        }
        this.evaluationScore = evaluationScore;
        this.managerComments = managerComments;
        this.status = KpiStatus.COMPLETED;
    }
    
    public void close() {
        if (this.status != KpiStatus.COMPLETED) {
            throw new InvalidStateException("KPI must be in COMPLETED state to be closed.");
        }
        this.status = KpiStatus.CLOSED;
    }

    /**
     * Forcefully closes a KPI, bypassing standard state transitions.
     * Used when an external event (e.g., Evaluation Cycle expiring) forces termination.
     */
    public void forceClose(String reason) {
        if (this.status == KpiStatus.CLOSED || this.status == KpiStatus.ARCHIVED) {
            return; // Already in a terminal state
        }
        this.status = KpiStatus.CLOSED;
        // The reason is passed for auditing purposes at the service layer.
    }

    public void updateProgress(BigDecimal newProgress) {
        if (this.status == KpiStatus.CLOSED || this.status == KpiStatus.ARCHIVED) {
            throw new InvalidStateException("Cannot update progress for a KPI in state: " + this.status);
        }
        this.currentProgress = newProgress;
    }

    public void updateFormula(String newFormula) {
        if (this.status != KpiStatus.DRAFT && this.status != KpiStatus.ACTIVE) {
            throw new BusinessRuleException("ERR_KPI_INVALID_STATE", "Cannot update formula unless DRAFT or ACTIVE");
        }
        this.formula = newFormula;
    }

    public void submitForApproval() {
        if (this.status != KpiStatus.DRAFT) {
            throw new BusinessRuleException("ERR_KPI_INVALID_STATE", "Only DRAFT KPIs can be submitted for approval");
        }
        this.status = KpiStatus.PENDING_MANAGER;
    }

    public void approveByManager() {
        if (this.status != KpiStatus.PENDING_MANAGER) {
            throw new BusinessRuleException("ERR_KPI_INVALID_STATE", "KPI must be in PENDING_MANAGER state");
        }
        // Short-circuit the workflow for the Demo: 
        // Skip PENDING_DIRECTOR and APPROVED, go straight to ACTIVE
        this.status = KpiStatus.ACTIVE;
    }

    public void approveByDirector() {
        if (this.status != KpiStatus.PENDING_DIRECTOR) {
            throw new BusinessRuleException("ERR_KPI_INVALID_STATE", "KPI must be in PENDING_DIRECTOR state");
        }
        this.status = KpiStatus.APPROVED;
    }

    // Getters for application/persistence layers
    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public UUID getTemplateId() { return templateId; }
    public UUID getOwnerId() { return ownerId; }
    public String getName() { return name; }
    public String getFrequency() { return frequency; }
    public KpiStatus getStatus() { return status; }
    public BigDecimal getTarget() { return target; }
    public BigDecimal getCurrentProgress() { return currentProgress; }
    public String getFormula() { return formula; }
    public BigDecimal getEvaluationScore() { return evaluationScore; }
    public String getManagerComments() { return managerComments; }
    public UUID getCycleId() { return cycleId; }
}
