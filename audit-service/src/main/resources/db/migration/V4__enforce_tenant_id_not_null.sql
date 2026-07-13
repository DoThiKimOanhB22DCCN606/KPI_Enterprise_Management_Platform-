-- Prevent the fallback tenant ID from being used in new audit logs
ALTER TABLE audit_logs ADD CONSTRAINT chk_tenant_id_not_fallback 
CHECK (tenant_id != '00000000-0000-0000-0000-000000000001');
