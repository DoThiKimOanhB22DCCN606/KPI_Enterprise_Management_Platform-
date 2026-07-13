package com.enterprise.kpi.application.service;

import com.enterprise.kpi.application.dto.AttachmentResponse;
import com.enterprise.kpi.infrastructure.config.TenantContext;
import com.enterprise.kpi.infrastructure.persistence.KpiAttachmentEntity;
import com.enterprise.kpi.infrastructure.persistence.KpiAttachmentRepository;
import com.enterprise.kpi.infrastructure.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KpiAttachmentService {

    private final StorageService storageService;
    private final KpiAttachmentRepository attachmentRepository;

    @Transactional
    public AttachmentResponse uploadAttachment(UUID kpiId, UUID valueId, MultipartFile file) throws Exception {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant context is required");
        }
        
        UUID attachmentId = UUID.randomUUID();
        String ext = getExtension(file.getOriginalFilename());
        String objectKey = String.format("%s/kpi-evidence/%s/%s%s", tenantId, valueId, attachmentId, ext);
        
        storageService.upload(objectKey, file);
        
        KpiAttachmentEntity entity = new KpiAttachmentEntity();
        entity.setId(attachmentId);
        entity.setTenantId(tenantId);
        entity.setKpiId(kpiId);
        entity.setValueId(valueId);
        entity.setObjectKey(objectKey);
        entity.setFileName(file.getOriginalFilename());
        entity.setFileSize(file.getSize());
        entity.setContentType(file.getContentType());
        entity.setUploadedAt(Instant.now());
        
        attachmentRepository.save(entity);
        
        return AttachmentResponse.builder()
            .id(attachmentId)
            .fileName(entity.getFileName())
            .url(storageService.generatePresignedUrl(objectKey))
            .build();
    }
    
    public List<AttachmentResponse> listAttachments(UUID valueId) {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant context is required");
        }
        return attachmentRepository.findByValueIdAndTenantId(valueId, tenantId).stream()
            .map(entity -> {
                try {
                    return AttachmentResponse.builder()
                        .id(entity.getId())
                        .fileName(entity.getFileName())
                        .url(storageService.generatePresignedUrl(entity.getObjectKey()))
                        .build();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            })
            .collect(Collectors.toList());
    }
    
    private String getExtension(String filename) {
        if (filename == null) return "";
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return filename.substring(lastDotIndex);
        }
        return "";
    }
}
