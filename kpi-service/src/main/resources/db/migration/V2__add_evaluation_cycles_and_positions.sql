-- V2__add_evaluation_cycles_and_positions.sql
-- Implements GAP-04 and GAP-06

-- Note: We use btree_gist (or just native tsrange) for GiST indexing on timestamps
CREATE EXTENSION IF NOT EXISTS btree_gist;

-- =========================================================================
-- GAP-04: Evaluation Cycles
-- =========================================================================

CREATE TABLE IF NOT EXISTS evaluation_cycles (
    id UUID PRIMARY KEY, -- Application should generate UUID v7
    tenant_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    period_start TIMESTAMP NOT NULL,
    period_end TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL,
    kpi_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    CONSTRAINT chk_eval_cycle_period CHECK (period_end > period_start)
);

-- Tenant isolation index
CREATE INDEX IF NOT EXISTS idx_evaluation_cycles_tenant ON evaluation_cycles(tenant_id);

-- GiST index for efficient period overlap queries
CREATE INDEX IF NOT EXISTS idx_evaluation_cycles_period_gist 
ON evaluation_cycles USING GIST (tsrange(period_start, period_end));

-- GIN index for full-text search on name
CREATE INDEX IF NOT EXISTS idx_evaluation_cycles_name_gin 
ON evaluation_cycles USING GIN (to_tsvector('english', name));

-- Link KPIs to the Evaluation Cycle (nullable for backwards compatibility)
ALTER TABLE kpis ADD COLUMN IF NOT EXISTS cycle_id UUID;

-- Optional foreign key constraint depending on multi-db/schema setup, but required structurally
ALTER TABLE kpis ADD CONSTRAINT fk_kpis_evaluation_cycle 
FOREIGN KEY (cycle_id) REFERENCES evaluation_cycles(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_kpis_cycle_id ON kpis(cycle_id);


-- =========================================================================
-- GAP-06: Positions (Job Titles)
-- =========================================================================

CREATE TABLE IF NOT EXISTS positions (
    id UUID PRIMARY KEY, -- Application should generate UUID v7
    tenant_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    department_id UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tenant isolation index
CREATE INDEX IF NOT EXISTS idx_positions_tenant ON positions(tenant_id);

-- GIN index for full-text search on position name
CREATE INDEX IF NOT EXISTS idx_positions_name_gin 
ON positions USING GIN (to_tsvector('english', name));

-- Safely add position_id FK to the users table if it exists in this schema.
-- Since Identity (Users) might reside in a different bounded context (auth-service), 
-- we use a DO block to ensure the migration passes without crashing if 'users' is missing here.
DO $$ 
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'users') THEN
        -- Check if column doesn't exist before adding to be idempotent
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'users' AND column_name = 'position_id') THEN
            ALTER TABLE users ADD COLUMN position_id UUID;
            ALTER TABLE users ADD CONSTRAINT fk_users_position FOREIGN KEY (position_id) REFERENCES positions(id) ON DELETE SET NULL;
        END IF;
        
        -- Create index if it doesn't exist
        IF NOT EXISTS (SELECT 1 FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace WHERE c.relname = 'idx_users_position_id') THEN
            CREATE INDEX idx_users_position_id ON users(position_id);
        END IF;
    END IF;
END $$;
