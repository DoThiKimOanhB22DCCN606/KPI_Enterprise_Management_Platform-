package com.enterprise.kpi.domain.exception;

public class DomainAuthorizationException extends RuntimeException {
    public DomainAuthorizationException(String message) {
        super(message);
    }
}
