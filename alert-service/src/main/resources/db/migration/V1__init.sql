CREATE TABLE IF NOT EXISTS alert_rules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    kpi_id UUID,
    condition_type VARCHAR(50),
    threshold_value NUMERIC(20,4),
    comparison_operator VARCHAR(20) DEFAULT 'LESS_THAN',
    severity VARCHAR(50) DEFAULT 'WARNING',
    notification_channel VARCHAR(50) DEFAULT 'IN_APP',
    enabled BOOLEAN DEFAULT TRUE,
    created_by UUID,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_alert_rules_tenant ON alert_rules(tenant_id);
CREATE INDEX IF NOT EXISTS idx_alert_rules_kpi ON alert_rules(kpi_id);

CREATE TABLE IF NOT EXISTS alert_instances (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    rule_id UUID NOT NULL,
    kpi_id UUID,
    triggered_value NUMERIC(20,4),
    threshold_value NUMERIC(20,4),
    severity VARCHAR(50),
    message TEXT,
    resolved BOOLEAN DEFAULT FALSE,
    resolved_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_alert_instances_rule ON alert_instances(rule_id);
CREATE INDEX IF NOT EXISTS idx_alert_instances_tenant ON alert_instances(tenant_id);
CREATE INDEX IF NOT EXISTS idx_alert_instances_created ON alert_instances(created_at);
