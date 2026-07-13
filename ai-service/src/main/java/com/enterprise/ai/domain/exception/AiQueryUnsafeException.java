package com.enterprise.ai.domain.exception;

public class AiQueryUnsafeException extends RuntimeException {
    public AiQueryUnsafeException(String message) {
        super("ERR_AI_QUERY_UNSAFE: " + message);
    }
    
    public AiQueryUnsafeException(String message, Throwable cause) {
        super("ERR_AI_QUERY_UNSAFE: " + message, cause);
    }
}
