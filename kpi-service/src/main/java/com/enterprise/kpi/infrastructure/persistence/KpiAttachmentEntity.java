package com.enterprise.kpi.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "kpi_attachments")
@Data
public class KpiAttachmentEntity {
    @Id
    private UUID id;
    private UUID tenantId;
    @Transient
    private UUID kpiId;

    @Column(name = "kpi_value_id")
    private UUID valueId;
    private String objectKey;
    private String fileName;
    private Long fileSize;
    private String contentType;
    private Instant uploadedAt;

}
