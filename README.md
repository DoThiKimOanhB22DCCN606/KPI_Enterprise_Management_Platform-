# 🚀 KEMP — Enterprise KPI & Performance Management Platform

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18.x-61DAFB.svg)](https://reactjs.org/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.x-3178C6.svg)](https://www.typescriptlang.org/)
[![Docker Compose](https://img.shields.io/badge/Docker-Compose-2496ED.svg)](https://www.docker.com/)
[![Kubernetes](https://img.shields.io/badge/Kubernetes-Ready-326CE5.svg)](https://kubernetes.io/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15+-336791.svg)](https://www.postgresql.org/)
[![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3.x-FF6600.svg)](https://www.rabbitmq.com/)

**KEMP (KPI Enterprise Management Platform)** is a production-ready, cloud-native, multi-tenant enterprise performance and objective management platform. It enables organizations to define hierarchical OKRs/KPIs, automate progress tracking through formula evaluation, trigger threshold-based alerting across omni-channel notifications (Email, Slack, Telegram, Zalo), build customizable drag-and-drop analytics dashboards, and leverage AI-driven Natural Language Text-to-SQL analytics with Row-Level Security (RLS).

---

## 📑 Table of Contents

1. [Key Features](#-key-features)
2. [System Architecture](#-system-architecture)
3. [Microservices Breakdown & Port Mapping](#-microservices-breakdown--port-mapping)
4. [Technology Stack](#-technology-stack)
5. [Event-Driven Architecture (RabbitMQ)](#-event-driven-architecture-rabbitmq)
6. [Multi-Tenancy & Security Model (RLS)](#-multi-tenancy--security-model-rls)
7. [Database Schema & Migrations](#-database-schema--migrations)
8. [Project Structure](#-project-structure)
9. [Getting Started & Local Setup](#-getting-started--local-setup)
   - [Prerequisites](#prerequisites)
   - [Quickstart with Docker Compose](#quickstart-with-docker-compose)
   - [Database Seeding](#database-seeding)
   - [Access URLs & Default Credentials](#access-urls--default-credentials)
10. [Frontend & Cypress E2E Testing](#-frontend--cypress-e2e-testing)
11. [AI-Powered Natural Language Analytics](#-ai-powered-natural-language-analytics)
12. [Observability & Monitoring](#-observability--monitoring)
13. [Kubernetes Deployment](#-kubernetes-deployment)
14. [Contributing & License](#-contributing--license)

---

## 🌟 Key Features

### 🏢 Multi-Tenant & Organization Management
* **Multi-Tenant Isolation**: Tenant scoping across HTTP headers, ThreadLocal context, and PostgreSQL Row-Level Security (RLS).
* **Tenant Theming & Subscriptions**: Per-tenant white-label branding, customized logos (via MinIO), and tier-based quota management.
* **Hierarchical & Matrix Organization**: Support for traditional hierarchical units, departments, matrix reporting, and cross-functional Agile project squads.

### 🎯 KPI Lifecycle & Dynamic Formula Engine
* **End-to-End KPI Lifecycle**: Proposal $\to$ Approval $\to$ Active tracking $\to$ Evaluation cycles $\to$ Completion / Archival.
* **Automated Formula Evaluation**: Dynamic formula parsing and calculation with target/actual threshold checks.
* **Evaluation Cycles**: Support for Monthly, Quarterly, Bi-Annual, and Annual evaluation periods with automatic scheduled state transitions.
* **Evidence & Attachments**: File attachments for target verification backed by MinIO object storage.

### 🌲 Goal Cascading & OKR Trees
* **Objective Alignment**: Top-level company goals cascade down to departments, squads, and individual targets.
* **Automated Recalculation**: Sub-goal and KPI progress updates trigger asynchronous tree recalculations across ancestors via RabbitMQ events.

### 📊 Dynamic Dashboards & Custom Widgets
* **Interactive Grid Builder**: Drag-and-drop dashboard composer powered by `react-grid-layout`.
* **Rich Widget Library**: Real-time KPI summary cards, Bar charts, Line trends, Gauge meters, and Leaderboards.
* **Dashboard Sharing & Caching**: Multi-user dashboard sharing with Redis-backed caching for sub-second load times.

### 🚨 Real-time Alerts & Omni-Channel Notifications
* **Rule Engine**: Evaluates progress drops, threshold breaches, and cycle deadlines.
* **Multi-Channel Dispatcher**: Real-time alerting delivered via **In-App**, **Email (SMTP)**, **Slack Webhooks**, **Telegram Bots**, and **Zalo OA**.
* **Escalation Levels**: Tiered severity handling (`INFO`, `WARNING`, `CRITICAL`).

### 🤖 AI-Powered Analytics & Text-to-SQL
* **Natural Language Querying**: Translates user questions (e.g., *"Show Q3 performance for Engineering Squad Alpha"*) into safe SQL queries.
* **AST Security Validation**: Whitelists safe SELECT statements and rejects unsafe DDL/DML manipulations.
* **Row-Level Security Enforcement**: Automatically binds `tenant_id` session variables to prevent multi-tenant data leakage.
* **AI Recommendation Engine**: Generates targeted performance improvement suggestions.

### 📄 Asynchronous Reporting & Comprehensive Auditing
* **Async Export Jobs**: Generate downloadable executive PDF and Excel reports without blocking HTTP threads.
* **Granular Audit Logs**: JSONB mutation history capturing previous vs. updated entity states.

---

## 🏛 System Architecture

```
                                  +-----------------------+
                                  |   Web Browser / SPA   |
                                  |    (React + Vite)     |
                                  +-----------+-----------+
                                              |
                                              v
                                  +-----------------------+
                                  |    gateway-service    |
                                  | (Port 8080 - Routing, |
                                  |  JWT, Rate Limiter)   |
                                  +-----------+-----------+
                                              |
     +-------------------+--------------------+-------------------+--------------------+
     |                   |                    |                   |                    |
     v                   v                    v                   v                    v
+-----------+     +-----------+        +-----------+       +-----------+        +-----------+
|web-bff-svc|     | auth-svc  |        | tenant-svc|       |  user-svc |        |  org-svc  |
| (Aggreg.) |     |  (OAuth/  |        |  (Themes/ |       | (RBAC/    |        | (Matrix/  |
|           |     |   MFA)    |        |   Subs)   |       |  Members) |        |  Squads)  |
+-----+-----+     +-----+-----+        +-----+-----+       +-----+-----+        +-----+-----+
      |                 |                    |                   |                    |
      +-----------------+--------------------+-------------------+--------------------+
                                              |
     +-------------------+--------------------+-------------------+--------------------+
     |                   |                    |                   |                    |
     v                   v                    v                   v                    v
+-----------+     +-----------+        +-----------+       +-----------+        +-----------+
|  kpi-svc  |     | goal-svc  |        | alert-svc |       |notif-svc  |        |report-svc |
| (Formula/ |     |  (Tree/   |        |  (Rules/  |       | (Slack/TG/|        | (PDF/XLSX |
|  Cycles)  |     |  Cascad.) |        |  Breach)  |       | Zalo/Mail)|        |  Async)   |
+-----+-----+     +-----+-----+        +-----+-----+       +-----+-----+        +-----+-----+
      |                 |                    |                   |                    |
      +-----------------+--------------------+-------------------+--------------------+
                                              |
                                 +------------+------------+
                                 |                         |
                                 v                         v
                          +-----------+             +-----------+
                          |analytics- |             |  ai-svc   |
                          |  service  |             |(Text-to-  |
                          | (Trends)  |             | SQL/Recom)|
                          +-----+-----+             +-----+-----+
                                |                         |
     ===========================v=========================v=============================
                                     SHARED INFRASTRUCTURE
     ===================================================================================
     +------------------+    +-------------------+    +----------------+    +----------+
     | PostgreSQL DB    |    | RabbitMQ Message  |    | Redis Cache &  |    | MinIO S3 |
     | (RLS Multi-Tenant|    | Broker (Events &  |    | Token Store    |    | Object   |
     |  16 Migrations)  |    |  Recalculations)  |    |                |    | Storage  |
     +------------------+    +-------------------+    +----------------+    +----------+
```

---

## 🔌 Microservices Breakdown & Port Mapping

| Service Name | Default Port | Primary Responsibility | Key Technologies |
| :--- | :---: | :--- | :--- |
| **`gateway-service`** | `8080` | API Gateway, Route resolution, Global JWT Auth, Rate Limiting | Spring Cloud Gateway, Reactive |
| **`auth-service`** | `8081` | Authentication, Token Issuing, Refresh Tokens, TOTP MFA | Spring Security, JJWT, Redis |
| **`user-service`** | `8082` | User lifecycle, Dynamic RBAC, Role permissions | Spring Data JPA, Mail Sender |
| **`tenant-service`** | `8083` | Multi-tenant organization provisioning, Subscriptions, Theming | Spring Data JPA, MinIO SDK |
| **`organization-service`** | `8084` | Org units hierarchy, departments, matrix reporting, project squads | Spring Data JPA |
| **`kpi-service`** | `8085` | KPI definitions, proposal workflows, formula evaluator, evaluation cycles | Spring Data JPA, Exp4j, MinIO SDK |
| **`goal-service`** | `8086` | Cascading goal trees, alignment mappings, auto-recalculation listeners | Spring Data JPA, RabbitMQ |
| **`dashboard-service`** | `8087` | User & team dashboards, widget layout configuration, layout caching | Spring Data JPA, Redis Cache |
| **`analytics-service`** | `8088` | Aggregated metrics, trend calculations, leaderboards, variance analysis | Spring Data JPA, Native SQL Views |
| **`alert-service`** | `8089` | Performance threshold evaluation, breach detection, alert lifecycle | Spring Data JPA, RabbitMQ |
| **`notification-service`** | `8090` | Omni-channel alert dispatching (Email, Slack, Telegram, Zalo, In-App) | Spring Web, RestTemplate, RabbitMQ |
| **`report-service`** | `8091` | Asynchronous PDF/Excel export generator, report queue consumer | Apache POI, OpenPDF, RabbitMQ |
| **`ai-service`** | `8092` | Natural language text-to-SQL, query security validator, AI recommendations | Spring AI / OpenAI Client, JSqlParser |
| **`web-bff-service`** | `8093` | Backend-For-Frontend aggregation layer for frontend views | Spring Cloud OpenFeign |
| **`audit-service`** | `8094` | Centralized tenant-level auditing, historical JSONB change tracker | Spring Data JPA, ShedLock |
| **`kemp-frontend`** | `5173` / `3000` | Single Page Application dashboard UI | React 18, Vite, TypeScript, Tailwind |

---

## 🛠 Technology Stack

### Backend Core
* **Language & Framework**: Java 17 / 21, Spring Boot 3.x
* **Security & Auth**: Spring Security 6, JJWT (HMAC-SHA256 / RSA), TOTP Multi-Factor Authentication
* **Inter-Service Communication**: Spring Cloud OpenFeign, Spring Cloud Gateway, Resilience4j
* **Event Broker**: RabbitMQ (AMQP 0-9-1) with dead-letter exchanges (DLX) and retry queues
* **Data & Persistence**: PostgreSQL 15+, Spring Data JPA, Hibernate 6, Flyway Migrations
* **Caching & Sessions**: Redis (Redis Template / Spring Cache)
* **Object Storage**: MinIO (S3-compatible bucket storage for logos, reports, and attachments)
* **Reporting Engine**: Apache POI (Excel generation), OpenPDF / iText (PDF generation)
* **AI Engine**: Spring AI / OpenAI API, JSqlParser (AST verification)

### Frontend Core
* **Framework**: React 18 with TypeScript
* **Build Tooling**: Vite, PostCSS, Tailwind CSS
* **UI Components & Icons**: Lucide Icons, Custom Design Tokens
* **Layout Engine**: React-Grid-Layout
* **Data Visualizations**: Recharts / Chart.js
* **Testing**: Cypress E2E Test Suite

### Observability & Infrastructure
* **Metrics & Monitoring**: Prometheus, Grafana, Micrometer
* **Containerization**: Docker, Docker Compose
* **Orchestration**: Kubernetes Manifests (Deployments, ClusterIP Services, Ingress Routes)

---

## 📬 Event-Driven Architecture (RabbitMQ)

The platform utilizes RabbitMQ for loose coupling and high-throughput asynchronous workloads:

```
[kpi-service] 
      │  (Event: kpi.progress.updated)
      ├───► [Exchange: kemp.events] 
                  │
                  ├───► [Queue: goal.recalculation.queue] ──► [goal-service]
                  │                                                │ (Goal recalculates & emits)
                  │                                                ▼ (Event: goal.recalculated)
                  │
                  ├───► [Queue: alert.evaluation.queue]    ──► [alert-service]
                  │                                                │ (Rule breaches detected)
                  │                                                ▼ (Event: alert.triggered)
                  │
                  └───► [Queue: audit.event.queue]         ──► [audit-service]

[alert-service]
      │  (Event: alert.triggered)
      └───► [Exchange: kemp.alerts]
                  │
                  └───► [Queue: notification.dispatch.queue] ──► [notification-service]
                                                                        ├──► Email Sender (SMTP)
                                                                        ├──► Slack Webhook
                                                                        ├──► Telegram Bot API
                                                                        ├──► Zalo OA API
                                                                        └──► In-App DB Storage
```

---

## 🔒 Multi-Tenancy & Security Model (RLS)

KEMP implements a **hybrid database multi-tenancy model** combining logical separation with database-enforced **PostgreSQL Row-Level Security (RLS)**:

1. **Context Propagation**:
   - `JwtAuthenticationFilter` inspects the bearer token and extracts `tenant_id` and `user_id`.
   - Populates `TenantContext` (backed by a scoped `ThreadLocal`).
2. **Database Session Scoping**:
   - Every connection acquired from the pool executes:
     ```sql
     SET LOCAL app.current_tenant_id = 'tenant-uuid-here';
     ```
3. **PostgreSQL RLS Policies**:
   - Tables across all schemas enforce RLS:
     ```sql
     ALTER TABLE kpis ENABLE ROW LEVEL SECURITY;
     CREATE POLICY tenant_isolation_policy ON kpis
       USING (tenant_id = current_setting('app.current_tenant_id')::uuid);
     ```
4. **AI Query Safeguards**:
   - The Text-to-SQL module automatically verifies that all generated queries strictly honor the RLS execution scope, preventing tenant data leaks.

---

## 🗄 Database Schema & Migrations

Database versioning is managed via Flyway migration scripts under `db/migration/`:

* `V1__init_extensions.sql` — PostgreSQL extensions (`uuid-ossp`, `pgcrypto`).
* `V2__identity_schema.sql` — Users, credentials, MFA tokens, and permissions.
* `V3__organization_schema.sql` — Org units, departments, matrix hierarchies, project squads.
* `V4__kpi_schema.sql` — KPI definitions, templates, progress history, evaluation cycles.
* `V5__goal_schema.sql` — Goals, alignment trees, OKR weightings.
* `V6__dashboard_schema.sql` — Dashboards, widgets, layout JSON definitions.
* `V7__notification_schema.sql` — Notifications, recipient mappings, channel settings.
* `V8__alert_schema.sql` — Alert rules, threshold configurations, triggered alert instances.
* `V9__audit_schema.sql` — Centralized audit trail with `JSONB` mutation diffs.
* `V10__integration_schema.sql` — API tokens, external webhooks, batch import jobs.
* `V11__ai_schema.sql` — AI conversational sessions and query interaction history.
* `V12__indexes.sql` — Performance composite indexes across tenant keys and timestamps.
* `V13__seed_permissions.sql` & `V14__seed_system_roles.sql` — Base RBAC roles and permissions.
* `V15__ai_query_rls.sql` — Dedicated read-only RLS policies for AI queries.
* `V16__analytics_views.sql` — Materialized views for trend aggregation and leaderboards.

---

## 📂 Project Structure

```
.
├── ai-service/                # Text-to-SQL generation & AI recommendation provider
├── alert-service/             # Threshold evaluation rule engine & breach detection
├── analytics-service/         # Aggregated trend reporting & leaderboard service
├── audit-service/             # Centralized audit logging & JSONB change tracking
├── auth-service/              # JWT auth, password management & TOTP MFA
├── dashboard-service/         # Dynamic dashboard composer & widget configurations
├── gateway-service/           # Spring Cloud API Gateway & global rate limiting
├── goal-service/              # Goal cascading trees & automatic progress recalculations
├── integration-service/       # Webhooks, API keys & batch CSV data imports
├── kemp-frontend/             # React 18 + Vite + Tailwind CSS single page application
│   ├── cypress/               # Cypress E2E test suites (builder, goals, kpis)
│   ├── src/
│   │   ├── api/               # Axios REST API client modules
│   │   ├── components/        # Reusable UI widgets, layout & modal components
│   │   ├── pages/             # Application route views (KPIs, OKRs, Admin, Cycles)
│   │   └── types/             # TypeScript domain definitions
├── kpi-service/               # Core KPI lifecycle, formula evaluation & cycles
├── notification-service/      # Omni-channel dispatcher (Slack, TG, Zalo, Mail)
├── organization-service/      # Org structures, matrix management & project squads
├── report-service/            # Asynchronous PDF & Excel generator
├── tenant-service/            # Tenant isolation, subscriptions & branding
├── user-service/              # User profiles, invitations & role RBAC
├── web-bff-service/           # Web Backend-For-Frontend aggregation layer
├── db/                        # Database initialization, migrations & seed scripts
├── k8s/                       # Kubernetes deployment manifests & ingress routes
├── monitoring/                # Prometheus configuration & Grafana dashboards
└── docker-compose.yml         # Full-stack local orchestration compose definition
```

---

## 🚀 Getting Started & Local Setup

### Prerequisites
* **Docker & Docker Compose** (Docker Desktop 20.10+ / Docker Compose v2+)
* **Java Development Kit (JDK 17 or 21)** (for local service compilation)
* **Node.js 18+ & npm / pnpm** (for local frontend development)
* **Maven 3.9+** (or use bundled `./mvnw`)

---

### Quickstart with Docker Compose

To boot up the entire ecosystem (all 14 microservices, PostgreSQL, RabbitMQ, Redis, MinIO, Prometheus, Grafana, and the Frontend):

```bash
# 1. Clone the repository
git clone https://github.com/dothikimoanhb22dccn606/kpi_enterprise_management_platform-.git
cd kpi_enterprise_management_platform-

# 2. Start all services via Docker Compose
docker compose up -d --build

# 3. Check status of running containers
docker compose ps
```

---

### Database Seeding

Once the database container is healthy and Flyway has executed migrations, run the sample seed data:

```bash
# Execute the comprehensive enterprise demo seed
docker exec -i kemp-postgres psql -U postgres -d kemp_db < db/enterprise_seed.sql

# (Optional) Seed CSAT and retail metrics
docker exec -i kemp-postgres psql -U postgres -d kemp_db < db/demo_csat_seed.sql
```

---

### Access URLs & Default Credentials

| Resource | URL | Default Credentials |
| :--- | :--- | :--- |
| **Web Frontend** | `http://localhost:5173` (or `http://localhost:3000`) | `admin@enterprise.com` / `Password123!` |
| **API Gateway** | `http://localhost:8080` | `Bearer <JWT_TOKEN>` |
| **RabbitMQ Management** | `http://localhost:15672` | `guest` / `guest` |
| **MinIO Console** | `http://localhost:9001` | `minioadmin` / `minioadmin` |
| **Grafana Monitoring** | `http://localhost:3001` | `admin` / `admin` |
| **Prometheus Metrics** | `http://localhost:9090` | *No auth required* |

---

## 🧪 Frontend & Cypress E2E Testing

The frontend is located under `kemp-frontend/` and comes configured with Cypress for comprehensive integration and end-to-end testing.

### Running Frontend Locally

```bash
cd kemp-frontend
npm install
npm run dev
```

### Running Cypress End-to-End Tests

```bash
cd kemp-frontend

# Run tests in headless mode
npx cypress run

# Or launch Cypress Interactive Test Runner
npx cypress open
```

**Included E2E Test Suites:**
* `dashboard_builder.cy.ts`: Verifies dynamic grid rearrangement, widget addition, and layout persistence.
* `goal_cascading.cy.ts`: Validates OKR tree rendering and parent progress recalculations.
* `kpi_workflow.cy.ts`: Covers KPI creation, proposal submission, approval actions, and progress value recording.

---

## 🤖 AI-Powered Natural Language Analytics

The `ai-service` enables executives and team leaders to query enterprise performance metrics using conversational plain English:

### Sample Request:
```http
POST /api/v1/ai/query
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
  "prompt": "Show top 5 squads with highest KPI completion rate this quarter"
}
```

### Process:
1. Intent extraction maps the prompt to SQL templates or LLM Text-to-SQL.
2. `QuerySecurityValidator` parses the generated AST to ensure it is strictly a read-only `SELECT` query.
3. Query executes within an isolated transaction scoped to the caller's `tenant_id`.
4. Response returns structured JSON data alongside chart recommendation metadata.

---

## 📈 Observability & Monitoring

The repository includes pre-configured monitoring assets located in `monitoring/`:

* **Prometheus**: Automatically scrapes Spring Boot Actuator endpoints (`/actuator/prometheus`) across all microservices.
* **Grafana**: Pre-provisioned datasources and dashboard templates (`kemp-overview.json`) providing insights into:
  - Microservice HTTP throughput and 95th/99th percentile response latencies.
  - RabbitMQ queue depth and message ingestion/consumption rates.
  - JVM garbage collection, heap utilization, and database connection pool saturation.

---

## ☸️ Kubernetes Deployment

Production deployment manifests are available in `k8s/`:

```bash
# Apply namespace and configurations
kubectl apply -f k8s/ingress.yaml
kubectl apply -f k8s/auth-deployment.yaml
kubectl apply -f k8s/gateway-deployment.yaml
kubectl apply -f k8s/kpi-deployment.yaml
```

---

## 👥 Contributing & License

1. Fork the repository.
2. Create a feature branch (`git checkout -b feature/amazing-feature`).
3. Commit your changes (`git commit -m 'feat: add amazing feature'`).
4. Push to the branch (`git push origin feature/amazing-feature`).
5. Open a Pull Request.

Distributed under the **MIT License**. See `LICENSE` for more information.
