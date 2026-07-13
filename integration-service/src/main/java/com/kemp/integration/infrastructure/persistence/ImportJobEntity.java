package com.kemp.integration.infrastructure.persistence;

import com.kemp.integration.domain.model.JobStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "import_jobs")
@Data
public class ImportJobEntity {
    @Id
    private UUID id;
    private UUID tenantId;
    private String fileName;
    private String fileType;
    
    @Enumerated(EnumType.STRING)
    private JobStatus status;
    
    private Long totalRows;
    private Long processedRows;
    private Long failedRows;
    
    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> errorDetails;
}
