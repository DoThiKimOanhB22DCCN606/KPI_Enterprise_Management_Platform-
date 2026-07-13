package com.enterprise.report.infrastructure.adapter.generator;

import com.enterprise.report.domain.model.ReportFormat;
import com.enterprise.report.domain.model.ReportRequest;
import com.enterprise.report.domain.port.ReportGeneratorPort;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.List;
import com.enterprise.report.infrastructure.adapter.client.KpiClient;
import com.enterprise.report.application.dto.KpiDTO;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPCell;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PdfReportGeneratorAdapter implements ReportGeneratorPort {

    private final KpiClient kpiClient;

    @Override
    public byte[] generateReport(ReportRequest request) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            
            document.open();
            String title = request.getTitle() != null ? request.getTitle() : "KEMP Summary Report";
            document.add(new Paragraph("Title: " + title));
            document.add(new Paragraph("Tenant ID: " + request.getTenantId().toString()));
            document.add(new Paragraph(" ")); // empty space
            
            // Fetch real data
            List<KpiDTO> kpis = kpiClient.getKpis(
                    request.getTenantId().toString(), 
                    request.getRequestingUserId().toString(), 
                    "SYSTEM", null, 0, 100).getContent();
                    
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.addCell("KPI Name");
            table.addCell("Status");
            table.addCell("Frequency");
            table.addCell("Current");
            table.addCell("Target");
            
            for (KpiDTO kpi : kpis) {
                table.addCell(kpi.getName() != null ? kpi.getName() : "Unnamed");
                table.addCell(kpi.getStatus() != null ? kpi.getStatus() : "-");
                table.addCell(kpi.getUpdateFrequency() != null ? kpi.getUpdateFrequency() : "-");
                table.addCell(kpi.getCurrentValue() != null ? kpi.getCurrentValue().toString() : "0");
                table.addCell(kpi.getTargetValue() != null ? kpi.getTargetValue().toString() : "0");
            }
            
            document.add(table);
            document.close();
            
            return out.toByteArray();
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }

    @Override
    public boolean supports(ReportFormat format) {
        return format == ReportFormat.PDF;
    }
}
