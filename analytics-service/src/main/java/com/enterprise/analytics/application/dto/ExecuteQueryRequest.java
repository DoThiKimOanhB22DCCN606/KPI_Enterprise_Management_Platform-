package com.enterprise.analytics.application.dto;

import lombok.Data;
import java.util.Map;

@Data
public class ExecuteQueryRequest {
    private String sql;
    private String tenantId;
    private Map<String, Object> parameters;
}
