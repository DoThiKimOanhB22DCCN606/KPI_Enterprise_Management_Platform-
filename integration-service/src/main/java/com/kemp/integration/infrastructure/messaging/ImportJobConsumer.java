package com.kemp.integration.infrastructure.messaging;

import com.kemp.integration.domain.model.ImportJob;
import com.kemp.integration.domain.model.JobStatus;
import com.kemp.integration.domain.repository.ImportJobRepository;
import com.kemp.integration.infrastructure.config.RabbitMQConfig;
import com.kemp.integration.infrastructure.config.TenantContext;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ImportJobConsumer {

    private final ImportJobRepository importJobRepository;
    private final ChunkProcessor chunkProcessor;
    
    @RabbitListener(queues = RabbitMQConfig.IMPORT_QUEUE)
    public void processJob(ImportJobMessage message) {
        try {
            TenantContext.setTenantId(message.getTenantId());
            
            ImportJob job = importJobRepository.findByIdAndTenantId(message.getJobId(), message.getTenantId())
                .orElseThrow(() -> new RuntimeException("Job not found"));
                
            job.setStatus(JobStatus.PROCESSING);
            importJobRepository.save(job);
            
            if ("CSV".equalsIgnoreCase(message.getFileType())) {
                processCsv(job, message.getFilePath());
            } else {
                job.setStatus(JobStatus.FAILED);
                job.setErrorDetails(List.of("Excel not fully implemented"));
            }
            
            job.setStatus(JobStatus.COMPLETED);
            importJobRepository.save(job);
            
        } catch (Exception e) {
            importJobRepository.findByIdAndTenantId(message.getJobId(), message.getTenantId()).ifPresent(job -> {
                job.setStatus(JobStatus.FAILED);
                List<String> errors = job.getErrorDetails();
                if (errors == null) errors = new ArrayList<>();
                errors.add(e.getMessage());
                job.setErrorDetails(errors);
                importJobRepository.save(job);
            });
        } finally {
            TenantContext.clear();
        }
    }

    private void processCsv(ImportJob job, String filePath) throws Exception {
        Reader in = new FileReader(filePath);
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in);
        
        List<CSVRecord> chunk = new ArrayList<>();
        long totalProcessed = 0;
        
        for (CSVRecord record : records) {
            chunk.add(record);
            if (chunk.size() >= 1000) {
                totalProcessed += chunkProcessor.process(chunk);
                chunk.clear();
                
                job.setProcessedRows(totalProcessed);
                importJobRepository.save(job);
            }
        }
        
        if (!chunk.isEmpty()) {
            totalProcessed += chunkProcessor.process(chunk);
            job.setProcessedRows(totalProcessed);
            importJobRepository.save(job);
        }
    }
}
