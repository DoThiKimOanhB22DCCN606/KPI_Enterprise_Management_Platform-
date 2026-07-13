package com.enterprise.kpi.interfaces.rest;

import com.enterprise.kpi.domain.exception.BusinessRuleException;
import com.enterprise.kpi.domain.exception.DomainAuthorizationException;
import com.enterprise.kpi.domain.exception.InvalidStateException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void businessRuleException_Returns422() {
        BusinessRuleException ex = new BusinessRuleException("ERR_KPI_IMMUTABLE", "Cannot modify a CLOSED KPI");
        ResponseEntity<Map<String, Object>> response = handler.handleBusinessRule(ex);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertEquals("ERR_KPI_IMMUTABLE", response.getBody().get("error"));
    }

    @Test
    void invalidStateException_Returns409() {
        InvalidStateException ex = new InvalidStateException("KPI must be APPROVED to activate");
        ResponseEntity<Map<String, Object>> response = handler.handleInvalidState(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("ERR_INVALID_STATE", response.getBody().get("error"));
    }

    @Test
    void domainAuthorizationException_Returns403() {
        DomainAuthorizationException ex = new DomainAuthorizationException("Owner cannot approve own KPI");
        ResponseEntity<Map<String, Object>> response = handler.handleDomainAuth(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("ERR_FORBIDDEN", response.getBody().get("error"));
    }

    @Test
    void runtimeException_NotFound_Returns404() {
        RuntimeException ex = new RuntimeException("KPI not found");
        ResponseEntity<Map<String, Object>> response = handler.handleException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("ERR_NOT_FOUND", response.getBody().get("error"));
    }

    @Test
    void runtimeException_Unexpected_Returns500() {
        RuntimeException ex = new RuntimeException("Unexpected DB failure");
        ResponseEntity<Map<String, Object>> response = handler.handleException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("ERR_INTERNAL", response.getBody().get("error"));
    }
}
