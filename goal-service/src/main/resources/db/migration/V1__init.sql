CREATE TABLE IF NOT EXISTS goals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    parent_goal_id UUID,
    kpi_id UUID,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    owner_type VARCHAR(50),
    owner_id UUID,
    target_value NUMERIC(20,4) NOT NULL,
    current_value NUMERIC(20,4) DEFAULT 0,
    progress_percent NUMERIC(8,2) DEFAULT 0,
    weight NUMERIC(5,2) DEFAULT 1.0,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    start_date DATE,
    end_date DATE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    created_by UUID,
    updated_by UUID,
    version BIGINT DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_goals_tenant ON goals(tenant_id);
CREATE INDEX IF NOT EXISTS idx_goals_parent ON goals(parent_goal_id);
CREATE INDEX IF NOT EXISTS idx_goals_owner ON goals(owner_type, owner_id);
CREATE INDEX IF NOT EXISTS idx_goals_status ON goals(status);
