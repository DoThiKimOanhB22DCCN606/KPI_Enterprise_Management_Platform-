package com.enterprise.kpi.application.dto;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class AttachmentResponse {
    private UUID id;
    private String fileName;
    private String url;
}
