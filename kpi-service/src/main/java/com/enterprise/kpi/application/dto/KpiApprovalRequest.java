package com.enterprise.kpi.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import com.enterprise.kpi.domain.model.ApprovalAction;

@Data
public class KpiApprovalRequest {
    @NotNull(message = "Action is required (SUBMIT, APPROVE, REJECT)")
    private ApprovalAction action;
    private String comment;
}
