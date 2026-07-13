ALTER TABLE kpis ADD COLUMN deleted_at TIMESTAMP;
ALTER TABLE kpi_values ADD COLUMN deleted_at TIMESTAMP;
ALTER TABLE kpi_attachments ADD COLUMN deleted_at TIMESTAMP;
ALTER TABLE evaluation_cycles ADD COLUMN deleted_at TIMESTAMP;
ALTER TABLE kpi_templates ADD COLUMN deleted_at TIMESTAMP;
