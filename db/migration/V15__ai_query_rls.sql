-- V15__ai_query_rls.sql
-- GAP-02: Layer 1 Postgres Row-Level Security

ALTER TABLE kpis ENABLE ROW LEVEL SECURITY;

-- If a policy already exists, we drop it to ensure idempotency in development, 
-- though Flyway runs exactly once.
DROP POLICY IF EXISTS tenant_isolation ON kpis;

CREATE POLICY tenant_isolation ON kpis
  FOR SELECT
  USING (tenant_id = current_setting('app.current_tenant_id')::uuid);
