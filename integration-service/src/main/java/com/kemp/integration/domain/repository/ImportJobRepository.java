package com.kemp.integration.domain.repository;

import com.kemp.integration.domain.model.ImportJob;
import java.util.Optional;
import java.util.UUID;

public interface ImportJobRepository {
    ImportJob save(ImportJob job);
    Optional<ImportJob> findByIdAndTenantId(UUID id, UUID tenantId);
}
