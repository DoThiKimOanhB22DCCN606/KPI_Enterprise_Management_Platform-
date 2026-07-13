package com.enterprise.kpi.application.service.port;

import java.util.UUID;

public interface OrganizationClientPort {
    /**
     * Checks if the approver is the direct manager of the employee.
     */
    boolean isDirectManager(UUID approverId, UUID employeeId);

    /**
     * Checks if the approver is the director of the employee (e.g., manager's manager).
     */
    boolean isDirector(UUID approverId, UUID employeeId);
}
