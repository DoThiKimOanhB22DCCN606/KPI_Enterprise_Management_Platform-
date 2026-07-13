package com.kemp.integration.application.dto;

import com.kemp.integration.domain.model.JobStatus;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ImportJobResponse {
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
