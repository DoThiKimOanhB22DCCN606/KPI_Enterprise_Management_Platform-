const fs = require('fs');
const crypto = require('crypto');

function gen_uuid() {
    return crypto.randomUUID();
}

function sql_escape(val) {
    if (val === null || val === undefined) return 'NULL';
    if (typeof val === 'boolean') return val ? 'true' : 'false';
    if (typeof val === 'number') return val.toString();
    return "'" + String(val).replace(/'/g, "''") + "'";
}

const out_file = 'd:/prj_vdt/db/large_demo_seed.sql';
let fd;
try {
    fd = fs.openSync(out_file, 'w');
} catch(e) {
    console.error(e);
    process.exit(1);
}

function write(sql) {
    fs.writeSync(fd, sql + "\n");
}

write("-- LARGE DEMO SEED SCRIPT");
write("-- DO NOT RUN IN PRODUCTION!");

write("DELETE FROM ai_messages;");
write("DELETE FROM alert_rules;");
write("DELETE FROM kpi_attachments;");
write("DELETE FROM kpi_values;");
write("DELETE FROM kpi_approvals;");
write("DELETE FROM kpis;");
write("DELETE FROM kpi_templates;");
write("DELETE FROM user_roles;");
write("DELETE FROM users;");
write("DELETE FROM roles;");
write("DELETE FROM organization_units;");
write("DELETE FROM tenants;");

const TENANT_ID = '00000000-0000-0000-0000-000000000001';
const ADMIN_ROLE = '00000000-0000-0000-0000-000000000003';
const HR_ROLE = '00000000-0000-0000-0000-000000000004';
const MANAGER_ROLE = '00000000-0000-0000-0000-000000000005';
const EMPLOYEE_ROLE = '00000000-0000-0000-0000-000000000006';
const DEFAULT_PASS = '$2a$10$irAD4mqsdBY886W69EBexOYqv8LeAP9ViX6c0RFKlGRtzgoDmWpRq';

write(`INSERT INTO tenants (id, code, name, status) VALUES (${sql_escape(TENANT_ID)}, 'SYS_TENANT', 'System Tenant', 'ACTIVE');`);

write(`INSERT INTO roles (id, tenant_id, code, name, system_role) VALUES `);
write(`(${sql_escape(ADMIN_ROLE)}, ${sql_escape(TENANT_ID)}, 'TENANT_ADMIN', 'Tenant Admin', true),`);
write(`(${sql_escape(HR_ROLE)}, ${sql_escape(TENANT_ID)}, 'HR_ADMIN', 'HR Admin', true),`);
write(`(${sql_escape(MANAGER_ROLE)}, ${sql_escape(TENANT_ID)}, 'STORE_MANAGER', 'Store Manager', true),`);
write(`(${sql_escape(EMPLOYEE_ROLE)}, ${sql_escape(TENANT_ID)}, 'EMPLOYEE', 'Employee', true);`);

const org_units = [];
const users = [];
const user_roles = [];
const kpi_templates = [];
const kpis = [];
const kpi_values = [];

// Org Hierarchy
const hq_id = gen_uuid();
org_units.push([hq_id, 'COMPANY', 'HQ', 'KEMP Corp HQ', 'HQ', 0, null]);

const regions = [
    [gen_uuid(), 'NORTH', 'North Region', 'HQ.NORTH'],
    [gen_uuid(), 'SOUTH', 'South Region', 'HQ.SOUTH']
];
for (const r of regions) {
    org_units.push([r[0], 'REGION', r[1], r[2], r[3], 1, hq_id]);
}

const stores = [];
for (let i = 1; i <= 10; i++) {
    const reg = regions[Math.floor(Math.random() * regions.length)];
    const s_id = gen_uuid();
    const path = `${reg[3]}.STORE_${i}`;
    const name = `Store ${i} (${reg[2]})`;
    stores.push([s_id, 'STORE', `STORE_${i}`, name, path, 2, reg[0]]);
    org_units.push(stores[stores.length - 1]);
}

const departments = [];
const dept_types = ['SALES', 'OPERATION', 'CSAT'];
for (const s of stores) {
    for (const dt of dept_types) {
        const d_id = gen_uuid();
        const path = `${s[4]}.${dt}`;
        const name = `${dt} Dept - ${s[3]}`;
        departments.push([d_id, 'DEPARTMENT', `${s[2]}_${dt}`, name, path, 3, s[0]]);
        org_units.push(departments[departments.length - 1]);
    }
}

write("INSERT INTO organization_units (id, tenant_id, type, code, name, path, level, parent_id) VALUES");
org_units.forEach((o, i) => {
    const end = i === org_units.length - 1 ? ";" : ",";
    write(`(${sql_escape(o[0])}, ${sql_escape(TENANT_ID)}, ${sql_escape(o[1])}, ${sql_escape(o[2])}, ${sql_escape(o[3])}, ${sql_escape(o[4])}, ${o[5]}, ${sql_escape(o[6])})${end}`);
});

const first_names = ['John', 'Jane', 'Michael', 'Sarah', 'David', 'Emma', 'Chris', 'Laura', 'Robert', 'Anna', 'James', 'Emily', 'William', 'Jessica', 'Joseph', 'Olivia', 'Charles', 'Sophia', 'Thomas', 'Ava'];
const last_names = ['Smith', 'Johnson', 'Williams', 'Brown', 'Jones', 'Garcia', 'Miller', 'Davis', 'Rodriguez', 'Martinez', 'Hernandez', 'Lopez', 'Gonzalez', 'Wilson', 'Anderson', 'Thomas', 'Taylor', 'Moore', 'Jackson', 'Martin'];

// Admin
const admin_id = gen_uuid();
users.push([admin_id, 'admin@kemp.com', 'Admin User', hq_id]);
user_roles.push([admin_id, ADMIN_ROLE]);

const all_employee_ids = [];
for (const d of departments) {
    const mgr_id = gen_uuid();
    const fnMgr = first_names[Math.floor(Math.random() * first_names.length)];
    const lnMgr = last_names[Math.floor(Math.random() * last_names.length)];
    const mgr_rnd = Math.floor(Math.random() * 900) + 100;
    users.push([mgr_id, `${fnMgr.toLowerCase()}.${lnMgr.toLowerCase()}.mgr${mgr_rnd}@kemp.com`, `${fnMgr} ${lnMgr} (Mgr)`, d[0]]);
    user_roles.push([mgr_id, MANAGER_ROLE]);
    
    const emp_count = Math.floor(Math.random() * 3) + 3; // 3 to 5
    for (let i = 0; i < emp_count; i++) {
        const e_id = gen_uuid();
        const fn = first_names[Math.floor(Math.random() * first_names.length)];
        const ln = last_names[Math.floor(Math.random() * last_names.length)];
        const rnd = Math.floor(Math.random() * 900) + 100;
        users.push([e_id, `${fn.toLowerCase()}.${ln.toLowerCase()}.${rnd}@kemp.com`, `${fn} ${ln}`, d[0]]);
        user_roles.push([e_id, EMPLOYEE_ROLE]);
        all_employee_ids.push(e_id);
    }
}

write("INSERT INTO users (id, tenant_id, email, password_hash, full_name, status, organization_unit_id) VALUES");
users.forEach((u, i) => {
    const end = i === users.length - 1 ? ";" : ",";
    write(`(${sql_escape(u[0])}, ${sql_escape(TENANT_ID)}, ${sql_escape(u[1])}, ${sql_escape(DEFAULT_PASS)}, ${sql_escape(u[2])}, 'ACTIVE', ${sql_escape(u[3])})${end}`);
});

write("INSERT INTO user_roles (user_id, role_id) VALUES");
user_roles.forEach((ur, i) => {
    const end = i === user_roles.length - 1 ? ";" : ",";
    write(`(${sql_escape(ur[0])}, ${sql_escape(ur[1])})${end}`);
});

const tpls = [
    [gen_uuid(), 'TPL_REV', 'Revenue Goal', 'SALES', 'MONTHLY', 'actual / target'],
    [gen_uuid(), 'TPL_CSAT', 'CSAT Score', 'CSAT', 'MONTHLY', 'actual / target'],
    [gen_uuid(), 'TPL_TKT', 'Tickets Resolved', 'OPERATION', 'WEEKLY', 'actual / target']
];

write("INSERT INTO kpi_templates (id, tenant_id, code, name, category, default_frequency, formula, active) VALUES");
tpls.forEach((t, i) => {
    const end = i === tpls.length - 1 ? ";" : ",";
    write(`(${sql_escape(t[0])}, ${sql_escape(TENANT_ID)}, ${sql_escape(t[1])}, ${sql_escape(t[2])}, ${sql_escape(t[3])}, ${sql_escape(t[4])}, ${sql_escape(t[5])}, true)${end}`);
});

function addDays(date, days) {
    const result = new Date(date);
    result.setDate(result.getDate() + days);
    return result;
}

const today = new Date();
const first_day_current_month = new Date(today.getFullYear(), today.getMonth(), 1);
const last_day_prev_month = new Date(first_day_current_month);
last_day_prev_month.setDate(0);
const first_day_prev_month = new Date(last_day_prev_month.getFullYear(), last_day_prev_month.getMonth(), 1);

const last_day_two_months_ago = new Date(first_day_prev_month);
last_day_two_months_ago.setDate(0);
const first_day_two_months_ago = new Date(last_day_two_months_ago.getFullYear(), last_day_two_months_ago.getMonth(), 1);

const periods = [
    [first_day_two_months_ago, last_day_two_months_ago, 'CLOSED', 'Month -2'],
    [first_day_prev_month, last_day_prev_month, 'CLOSED', 'Month -1'],
    [first_day_current_month, new Date(today.getFullYear(), today.getMonth() + 1, 0), 'ACTIVE', 'Current Month']
];

const bad_performers = new Set();
while(bad_performers.size < Math.max(1, Math.floor(all_employee_ids.length / 5))) {
    bad_performers.add(all_employee_ids[Math.floor(Math.random() * all_employee_ids.length)]);
}

function formatDate(d) {
    return d.toISOString().split('T')[0];
}

const kpis_chunk = [];
const values_chunk = [];

for (const emp_id of all_employee_ids) {
    for (const period of periods) {
        const start_dt = period[0];
        const end_dt = period[1];
        const status = period[2];
        
        const target = Math.floor(Math.random() * 900) + 100;
        let actual;
        
        if (bad_performers.has(emp_id) && status === 'CLOSED') {
            actual = target * (0.3 + Math.random() * 0.4); // Bad
        } else {
            actual = target * (0.7 + Math.random() * 0.5); // Normal to Good
        }
            
        const progress = (actual / target) * 100;
        
        const k_id = gen_uuid();
        const tpl = tpls[Math.floor(Math.random() * tpls.length)];
        
        kpis_chunk.push([k_id, tpl[0], `${tpl[2]} - ${period[3]}`, emp_id, target, status, formatDate(start_dt), formatDate(end_dt), progress]);
        
        const daysDiff = Math.floor((end_dt - start_dt) / (1000 * 60 * 60 * 24));
        const step = Math.max(1, Math.floor(daysDiff / 3));
        
        let curr_actual = 0;
        for (let step_idx = 1; step_idx <= 3; step_idx++) {
            let v_date = addDays(start_dt, step * step_idx);
            if (v_date > today) {
                v_date = today;
            }
            
            const chunk_actual = actual / 3;
            curr_actual += chunk_actual;
            const curr_prog = (curr_actual / target) * 100;
            
            const v_id = gen_uuid();
            values_chunk.push([v_id, k_id, formatDate(start_dt), formatDate(v_date), curr_actual, curr_prog, 'Progress update']);

            if (v_date.getTime() === today.getTime()) {
                break;
            }
        }
    }
}

write("INSERT INTO kpis (id, tenant_id, template_id, name, owner_type, owner_id, frequency, target_value, status, start_date, end_date, current_progress) VALUES");
kpis_chunk.forEach((k, i) => {
    const end = i === kpis_chunk.length - 1 ? ";" : ",";
    write(`(${sql_escape(k[0])}, ${sql_escape(TENANT_ID)}, ${sql_escape(k[1])}, ${sql_escape(k[2])}, 'USER', ${sql_escape(k[3])}, 'MONTHLY', ${k[4]}, ${sql_escape(k[5])}, '${k[6]}', '${k[7]}', ${k[8].toFixed(2)})${end}`);
});

write("INSERT INTO kpi_values (id, tenant_id, kpi_id, period_start, period_end, actual_value, progress_percent, comment) VALUES");
values_chunk.forEach((v, i) => {
    const end = i === values_chunk.length - 1 ? ";" : ",";
    write(`(${sql_escape(v[0])}, ${sql_escape(TENANT_ID)}, ${sql_escape(v[1])}, '${v[2]}', '${v[3]}', ${v[4].toFixed(2)}, ${v[5].toFixed(2)}, ${sql_escape(v[6])})${end}`);
});

write("INSERT INTO alert_rules (id, tenant_id, name, description, condition_type, threshold_value, comparison_operator, severity, enabled) VALUES");
write(`(${sql_escape(gen_uuid())}, ${sql_escape(TENANT_ID)}, 'Low Progress Alert', 'Alerts when progress is below 50%', 'PROGRESS', 50.0, 'LESS_THAN', 'CRITICAL', true);`);

fs.closeSync(fd);
console.log(`Generated seed file successfully at ${out_file}`);
