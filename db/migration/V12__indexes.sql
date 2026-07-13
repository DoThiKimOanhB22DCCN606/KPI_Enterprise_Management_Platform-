CREATE INDEX idx_kpis_tenant_status ON kpis(tenant_id, status);
CREATE INDEX idx_kpis_period ON kpis(start_date, end_date);
CREATE INDEX idx_kpi_values_period_kpi ON kpi_values(kpi_id, period_start);
CREATE INDEX idx_goals_tenant_status ON goals(tenant_id, status);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);
