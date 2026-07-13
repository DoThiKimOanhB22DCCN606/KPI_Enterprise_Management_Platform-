CREATE TABLE IF NOT EXISTS dashboard_widgets (
    id UUID PRIMARY KEY,
    dashboard_id UUID NOT NULL,
    widget_type VARCHAR(255) NOT NULL,
    title VARCHAR(255),
    x INTEGER,
    y INTEGER,
    width INTEGER,
    height INTEGER,
    config_json JSONB,
    CONSTRAINT fk_dashboard
        FOREIGN KEY(dashboard_id) 
        REFERENCES dashboards(id)
        ON DELETE CASCADE
);
