export interface User {
  id: string;
  fullName: string;
  email: string;
  role: string;
}

export interface Organization {
  id: string;
  name: string;
  type: string;
}

export interface KpiSummary {
  id: string;
  templateName: string;
  status: string;
  target: number;
  currentProgress: number;
}

export interface DashboardViewDTO {
  user: User;
  organization: Organization;
  kpis: KpiSummary[];
  overallProgress: number;
}
