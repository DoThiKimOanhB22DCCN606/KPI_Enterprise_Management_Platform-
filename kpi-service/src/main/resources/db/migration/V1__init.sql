CREATE TABLE IF NOT EXISTS kpi_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    code VARCHAR(100),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(100),
    default_frequency VARCHAR(50),
    formula TEXT,
    unit VARCHAR(50),
    target_value NUMERIC(20,4),
    threshold_green NUMERIC(20,4),
    threshold_yellow NUMERIC(20,4),
    threshold_red NUMERIC(20,4),
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    created_by UUID,
    version BIGINT DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_kpi_templates_tenant ON kpi_templates(tenant_id);
CREATE INDEX IF NOT EXISTS idx_kpi_templates_category ON kpi_templates(category);

CREATE TABLE IF NOT EXISTS kpis (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    template_id UUID,
    code VARCHAR(100),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    owner_type VARCHAR(50),
    owner_id UUID,
    unit VARCHAR(50),
    frequency VARCHAR(50) NOT NULL DEFAULT 'MONTHLY',
    target_value NUMERIC(20,4),
    current_value NUMERIC(20,4) DEFAULT 0,
    weight NUMERIC(5,2) DEFAULT 1.0,
    threshold_green NUMERIC(20,4),
    threshold_yellow NUMERIC(20,4),
    threshold_red NUMERIC(20,4),
    formula TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    start_date DATE,
    end_date DATE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    created_by UUID,
    updated_by UUID,
    version BIGINT DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_kpis_tenant ON kpis(tenant_id);
CREATE INDEX IF NOT EXISTS idx_kpis_owner ON kpis(owner_type, owner_id);
CREATE INDEX IF NOT EXISTS idx_kpis_status ON kpis(status);
CREATE INDEX IF NOT EXISTS idx_kpis_period ON kpis(start_date, end_date);

CREATE TABLE IF NOT EXISTS kpi_values (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    kpi_id UUID NOT NULL,
    period_start DATE,
    period_end DATE,
    actual_value NUMERIC(20,4),
    progress_percent NUMERIC(8,2),
    comment TEXT,
    created_by UUID,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    version BIGINT DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_kpi_values_kpi ON kpi_values(kpi_id);
CREATE INDEX IF NOT EXISTS idx_kpi_values_period ON kpi_values(period_start);
CREATE INDEX IF NOT EXISTS idx_kpi_values_tenant ON kpi_values(tenant_id);

CREATE TABLE IF NOT EXISTS kpi_attachments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    kpi_value_id UUID NOT NULL,
    file_name VARCHAR(255),
    object_key TEXT,
    uploaded_at TIMESTAMP DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_kpi_attachments_value ON kpi_attachments(kpi_value_id);

CREATE TABLE IF NOT EXISTS kpi_approvals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    kpi_id UUID NOT NULL,
    approver_id UUID NOT NULL,
    action VARCHAR(50) NOT NULL,
    comment TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_kpi_approvals_kpi ON kpi_approvals(kpi_id);
