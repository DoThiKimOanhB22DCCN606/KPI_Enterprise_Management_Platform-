CREATE TABLE goals (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    parent_goal_id UUID REFERENCES goals(id) ON DELETE SET NULL,
    kpi_id UUID,
    owner_type VARCHAR(50),
    owner_id UUID,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    target_value NUMERIC(20,4) NOT NULL,
    current_value NUMERIC(20,4) DEFAULT 0,
    overall_progress NUMERIC(8,2) DEFAULT 0,
    weight DECIMAL(5,4) DEFAULT 1.0 NOT NULL,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    expected_date TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    created_by UUID,
    updated_by UUID,
    version BIGINT DEFAULT 0
);

CREATE INDEX idx_goals_parent ON goals(parent_goal_id);
CREATE INDEX idx_goals_owner ON goals(owner_type, owner_id);
CREATE INDEX idx_goals_tenant ON goals(tenant_id);
