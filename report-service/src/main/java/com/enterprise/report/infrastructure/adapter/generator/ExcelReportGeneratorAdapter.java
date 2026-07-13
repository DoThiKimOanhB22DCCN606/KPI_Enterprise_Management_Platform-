package com.enterprise.report.infrastructure.adapter.generator;

import com.enterprise.report.domain.model.ReportFormat;
import com.enterprise.report.domain.model.ReportRequest;
import com.enterprise.report.domain.port.ReportGeneratorPort;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import com.enterprise.report.infrastructure.adapter.client.KpiClient;
import com.enterprise.report.application.dto.KpiDTO;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ExcelReportGeneratorAdapter implements ReportGeneratorPort {

    private final KpiClient kpiClient;

    @Override
    public byte[] generateReport(ReportRequest request) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("Report Data");
            
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("KPI Name");
            headerRow.createCell(1).setCellValue("Status");
            headerRow.createCell(2).setCellValue("Frequency");
            headerRow.createCell(3).setCellValue("Current Value");
            headerRow.createCell(4).setCellValue("Target Value");
            
            // Fetch real data
            List<KpiDTO> kpis = kpiClient.getKpis(
                    request.getTenantId().toString(), 
                    request.getRequestingUserId().toString(), 
                    "SYSTEM", null, 0, 100).getContent();
            
            int rowNum = 1;
            for (KpiDTO kpi : kpis) {
                Row dataRow = sheet.createRow(rowNum++);
                dataRow.createCell(0).setCellValue(kpi.getName() != null ? kpi.getName() : "Unnamed");
                dataRow.createCell(1).setCellValue(kpi.getStatus() != null ? kpi.getStatus() : "-");
                dataRow.createCell(2).setCellValue(kpi.getUpdateFrequency() != null ? kpi.getUpdateFrequency() : "-");
                dataRow.createCell(3).setCellValue(kpi.getCurrentValue() != null ? kpi.getCurrentValue().doubleValue() : 0.0);
                dataRow.createCell(4).setCellValue(kpi.getTargetValue() != null ? kpi.getTargetValue().doubleValue() : 0.0);
            }

            workbook.write(out);
            return out.toByteArray();
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel report", e);
        }
    }

    @Override
    public boolean supports(ReportFormat format) {
        return format == ReportFormat.EXCEL;
    }
}
