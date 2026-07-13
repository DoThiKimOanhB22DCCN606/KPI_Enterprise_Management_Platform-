package com.kemp.integration.application.service;

import com.kemp.integration.application.dto.ImportJobResponse;
import com.kemp.integration.domain.model.ImportJob;
import com.kemp.integration.domain.model.JobStatus;
import com.kemp.integration.domain.repository.ImportJobRepository;
import com.kemp.integration.infrastructure.config.TenantContext;
import com.kemp.integration.infrastructure.messaging.ImportJobMessage;
import com.kemp.integration.infrastructure.messaging.ImportJobPublisher;
import java.io.File;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ImportService {

    private final ImportJobRepository importJobRepository;
    private final ImportJobPublisher publisher;

    @Transactional
    public UUID uploadFile(MultipartFile file, String type) throws Exception {
        File tempFile = File.createTempFile("import-", "." + type.toLowerCase());
        file.transferTo(tempFile);
        
        ImportJob job = ImportJob.builder()
            .tenantId(getTenantId())
            .fileName(file.getOriginalFilename())
            .fileType(type.toUpperCase())
            .status(JobStatus.PENDING)
            .totalRows(0L)
            .processedRows(0L)
            .failedRows(0L)
            .build();
            
        job = importJobRepository.save(job);
        
        ImportJobMessage message = ImportJobMessage.builder()
            .jobId(job.getId())
            .tenantId(job.getTenantId())
            .filePath(tempFile.getAbsolutePath())
            .fileType(type.toUpperCase())
            .build();
            
        publisher.publish(message);
        return job.getId();
    }

    public ImportJobResponse getJobStatus(UUID jobId) {
        ImportJob job = importJobRepository.findByIdAndTenantId(jobId, getTenantId())
            .orElseThrow(() -> new RuntimeException("Job not found"));
            
        return ImportJobResponse.builder()
            .id(job.getId())
            .tenantId(job.getTenantId())
            .fileName(job.getFileName())
            .fileType(job.getFileType())
            .status(job.getStatus())
            .totalRows(job.getTotalRows())
            .processedRows(job.getProcessedRows())
            .failedRows(job.getFailedRows())
            .errorDetails(job.getErrorDetails())
            .build();
    }

    private UUID getTenantId() {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) throw new RuntimeException("TenantContext missing");
        return tenantId;
    }
}
