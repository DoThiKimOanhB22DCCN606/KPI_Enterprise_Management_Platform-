package com.kemp.integration.infrastructure.messaging;

import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ImportJobMessage {
    private UUID jobId;
    private UUID tenantId;
    private String filePath;
    private String fileType;
}
