package com.kemp.integration.infrastructure.persistence;

import com.kemp.integration.domain.model.ImportJob;
import com.kemp.integration.domain.repository.ImportJobRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ImportJobRepositoryAdapter implements ImportJobRepository {
    
    private final ImportJobJpaRepository repository;

    @Override
    public ImportJob save(ImportJob job) {
        ImportJobEntity entity = toEntity(job);
        return toDomain(repository.save(entity));
    }

    @Override
    public Optional<ImportJob> findByIdAndTenantId(UUID id, UUID tenantId) {
        return repository.findByIdAndTenantId(id, tenantId).map(this::toDomain);
    }
    
    private ImportJobEntity toEntity(ImportJob domain) {
        ImportJobEntity entity = new ImportJobEntity();
        entity.setId(domain.getId() == null ? UUID.randomUUID() : domain.getId());
        entity.setTenantId(domain.getTenantId());
        entity.setFileName(domain.getFileName());
        entity.setFileType(domain.getFileType());
        entity.setStatus(domain.getStatus());
        entity.setTotalRows(domain.getTotalRows());
        entity.setProcessedRows(domain.getProcessedRows());
        entity.setFailedRows(domain.getFailedRows());
        entity.setErrorDetails(domain.getErrorDetails());
        return entity;
    }
    
    private ImportJob toDomain(ImportJobEntity entity) {
        return ImportJob.builder()
            .id(entity.getId())
            .tenantId(entity.getTenantId())
            .fileName(entity.getFileName())
            .fileType(entity.getFileType())
            .status(entity.getStatus())
            .totalRows(entity.getTotalRows())
            .processedRows(entity.getProcessedRows())
            .failedRows(entity.getFailedRows())
            .errorDetails(entity.getErrorDetails())
            .build();
    }
}
