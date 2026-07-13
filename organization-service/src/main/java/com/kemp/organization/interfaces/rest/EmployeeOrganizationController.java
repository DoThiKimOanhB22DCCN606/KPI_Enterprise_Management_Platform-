package com.kemp.organization.interfaces.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/v1/organizations/employees")
@RequiredArgsConstructor
public class EmployeeOrganizationController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/{employeeId}/manager/{approverId}")
    public ResponseEntity<Boolean> isDirectManager(
            @PathVariable UUID employeeId,
            @PathVariable UUID approverId) {
        
        String sql = """
            SELECT ou.manager_user_id 
            FROM users u 
            JOIN organization_units ou ON u.organization_unit_id = ou.id 
            WHERE u.id = ? AND u.deleted_at IS NULL AND ou.deleted_at IS NULL
            """;
            
        try {
            UUID managerId = jdbcTemplate.queryForObject(sql, UUID.class, employeeId);
            return ResponseEntity.ok(approverId.equals(managerId));
        } catch (Exception e) {
            log.warn("Failed to determine direct manager for employee {}: {}", employeeId, e.getMessage());
            return ResponseEntity.ok(false);
        }
    }

    @GetMapping("/{employeeId}/director/{approverId}")
    public ResponseEntity<Boolean> isDirector(
            @PathVariable UUID employeeId,
            @PathVariable UUID approverId) {
        
        // Find if approverId is a manager of ANY ancestor org unit (or the employee's own org unit)
        String sql = """
            SELECT count(*) 
            FROM organization_units ou_ancestor
            JOIN (
                SELECT ou.path 
                FROM users u 
                JOIN organization_units ou ON u.organization_unit_id = ou.id 
                WHERE u.id = ? AND u.deleted_at IS NULL AND ou.deleted_at IS NULL
            ) target_ou ON target_ou.path <@ ou_ancestor.path
            WHERE ou_ancestor.manager_user_id = ? AND ou_ancestor.deleted_at IS NULL
            """;
            
        try {
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, employeeId, approverId);
            return ResponseEntity.ok(count != null && count > 0);
        } catch (Exception e) {
            log.warn("Failed to determine director for employee {}: {}", employeeId, e.getMessage());
            return ResponseEntity.ok(false);
        }
    }
}
