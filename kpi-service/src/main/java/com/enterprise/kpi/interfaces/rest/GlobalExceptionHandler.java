package com.enterprise.kpi.interfaces.rest;

import com.enterprise.kpi.domain.exception.BusinessRuleException;
import com.enterprise.kpi.domain.exception.DomainAuthorizationException;
import com.enterprise.kpi.domain.exception.InvalidStateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles domain business rule violations.
     * Example: submitting a non-DRAFT KPI, updating a CLOSED KPI.
     * HTTP 422 Unprocessable Entity — the request was understood but violates a rule.
     */
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessRule(BusinessRuleException ex) {
        log.warn("Business rule violation: [{}] {}", ex.getCode(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of(
                "error", ex.getCode(),
                "message", ex.getMessage(),
                "timestamp", Instant.now().toString()
        ));
    }

    /**
     * Handles invalid state transitions in the domain model.
     * Example: activating a KPI that is not APPROVED.
     * HTTP 409 Conflict.
     */
    @ExceptionHandler(InvalidStateException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidState(InvalidStateException ex) {
        log.warn("Invalid state transition: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "error", "ERR_INVALID_STATE",
                "message", ex.getMessage(),
                "timestamp", Instant.now().toString()
        ));
    }

    /**
     * Handles authorization failures at the domain level.
     * Example: non-owner trying to submit, owner trying to approve own KPI.
     * HTTP 403 Forbidden.
     */
    @ExceptionHandler(DomainAuthorizationException.class)
    public ResponseEntity<Map<String, Object>> handleDomainAuth(DomainAuthorizationException ex) {
        log.warn("Domain authorization failure: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "error", "ERR_FORBIDDEN",
                "message", ex.getMessage(),
                "timestamp", Instant.now().toString()
        ));
    }

    /**
     * Handles entity not found (currently thrown as RuntimeException with "KPI not found").
     * HTTP 404 Not Found.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception ex) {
        String message = ex.getMessage();
        if (message != null && message.contains("not found")) {
            log.warn("Resource not found: {}", message);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "error", "ERR_NOT_FOUND",
                    "message", message,
                    "timestamp", Instant.now().toString()
            ));
        }
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "ERR_INTERNAL",
                "message", "An unexpected error occurred",
                "timestamp", Instant.now().toString()
        ));
    }

    /**
     * Handles Jakarta Validation failures (@Valid on request bodies).
     * HTTP 400 Bad Request.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("Validation failed: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", "ERR_VALIDATION",
                "message", errors,
                "timestamp", Instant.now().toString()
        ));
    }
}
