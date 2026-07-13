package com.kemp.integration.domain.model;

import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ImportJob {
    private UUID id;
    private UUID tenantId;
    private String fileName;
    private String fileType;
    private JobStatus status;
    private Long totalRows;
    private Long processedRows;
    private Long failedRows;
    private List<String> errorDetails;
}
