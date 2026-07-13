import { apiClient } from '../lib/apiClient';

export interface TrendDataPoint {
  period: string;
  value: number;
  target?: number;
}

export interface KpiPerformance {
  kpiId: string;
  currentValue: number;
  targetValue: number;
  performancePct: number;
}

export interface LeaderboardEntry {
  entityId: string;
  entityName: string;
  score: number;
  rank: number;
}

export const analyticsApi = {
  getKpiTrend: async (kpiId: string): Promise<TrendDataPoint[]> => {
    const res = await apiClient.get(`/v1/analytics/kpis/${kpiId}/trend`);
    return res.data;
  },

  getKpiPerformance: async (kpiId: string): Promise<KpiPerformance> => {
    const res = await apiClient.get(`/v1/analytics/kpis/${kpiId}/performance`);
    return res.data;
  },

  getLeaderboard: async (type: string, period: string, limit: number = 10): Promise<LeaderboardEntry[]> => {
    const res = await apiClient.get(`/v1/analytics/leaderboard/${type}`, {
      params: { period, limit },
    });
    return res.data;
  },
};
