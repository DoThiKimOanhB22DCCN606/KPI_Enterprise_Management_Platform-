package com.enterprise.report.infrastructure.adapter.storage;

import com.enterprise.report.domain.port.ReportStoragePort;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Component
public class LocalFileSystemStorageAdapter implements ReportStoragePort {

    @Value("${report.storage.path}")
    private String storagePath;

    @Value("${report.storage.base-url}")
    private String baseUrl;

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(storagePath));
        } catch (IOException e) {
            log.warn("Could not create temp storage directory. It may already exist.", e);
        }
    }

    @Override
    public String saveReport(String requestId, byte[] content, String fileExtension) {
        try {
            String filename = requestId + "." + fileExtension;
            Path filePath = Paths.get(storagePath, filename);
            Files.write(filePath, content);
            
            log.info("Saved report to local filesystem: {}", filePath.toAbsolutePath());
            
            return baseUrl + filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save report to local storage", e);
        }
    }
}
