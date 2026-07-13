CREATE TABLE kpi_templates (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    code VARCHAR(100),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(100) NOT NULL,
    default_frequency VARCHAR(50) NOT NULL,
    formula TEXT,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    created_by UUID,
    updated_by UUID,
    version BIGINT DEFAULT 0
);

CREATE TABLE kpis (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    template_id UUID REFERENCES kpi_templates(id) ON DELETE RESTRICT,
    code VARCHAR(100),
    name VARCHAR(255),
    description TEXT,
    owner_type VARCHAR(50),
    owner_id UUID,
    unit VARCHAR(50),
    frequency VARCHAR(50) NOT NULL,
    target_value NUMERIC(20,4),
    weight NUMERIC(5,2) DEFAULT 1.0,
    threshold_green NUMERIC(20,4),
    threshold_yellow NUMERIC(20,4),
    threshold_red NUMERIC(20,4),
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    start_date DATE,
    end_date DATE,
    current_progress NUMERIC(20,4) DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    created_by UUID,
    updated_by UUID,
    version BIGINT DEFAULT 0
);

CREATE TABLE kpi_approvals (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    kpi_id UUID NOT NULL REFERENCES kpis(id) ON DELETE CASCADE,
    approver_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    comments TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    version BIGINT DEFAULT 0
);

CREATE TABLE kpi_values (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    kpi_id UUID NOT NULL REFERENCES kpis(id) ON DELETE CASCADE,
    period_start TIMESTAMPTZ NOT NULL,
    period_end TIMESTAMPTZ NOT NULL,
    actual_value NUMERIC(20,4),
    progress_percent NUMERIC(8,2),
    evidence JSONB,
    comment TEXT,
    created_by UUID,
    created_at TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    version BIGINT DEFAULT 0
);

CREATE TABLE kpi_attachments (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    kpi_value_id UUID NOT NULL REFERENCES kpi_values(id) ON DELETE CASCADE,
    file_name VARCHAR(255),
    object_key TEXT NOT NULL,
    content_type VARCHAR(100),
    file_size BIGINT,
    uploaded_by UUID,
    uploaded_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_kpis_owner ON kpis(owner_type, owner_id);
CREATE INDEX idx_kpis_status ON kpis(status);
CREATE INDEX idx_kpis_tenant ON kpis(tenant_id);
CREATE INDEX idx_kpi_values_kpi ON kpi_values(kpi_id);
CREATE INDEX idx_kpi_values_period ON kpi_values(period_start);
CREATE INDEX idx_kpi_values_evidence ON kpi_values USING GIN(evidence);
