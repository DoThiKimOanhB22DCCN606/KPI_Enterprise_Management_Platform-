import React, { useEffect, useState } from 'react';
import type { DashboardViewDTO, KpiSummary } from '../types';
import { Target, TrendingUp, Clock, Building, User as UserIcon } from 'lucide-react';
import clsx from 'clsx';

export const Dashboard: React.FC = () => {
  const [data, setData] = useState<DashboardViewDTO | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // In MVP, we hardcode the target URL. In production, this would be an env var.
    fetch('http://localhost:8080/v1/bff/dashboard/summary?userId=00000000-0000-0000-0000-000000000001&orgId=00000000-0000-0000-0000-000000000001')
      .then(res => {
        if (!res.ok) throw new Error('Failed to fetch dashboard');
        return res.json();
      })
      .then((payload) => setData(payload))
      .catch((error) => {
        console.warn('Backend unavailable, falling back to mock data:', error);
        // Fallback Mock Data
        setData({
          user: { id: '1', fullName: 'Alice Jenkins', email: 'alice.j@enterprise.com', role: 'MANAGER' },
          organization: { id: '1', name: 'Global Sales & Marketing', type: 'DEPARTMENT' },
          overallProgress: 68.5,
          kpis: [
            { id: '1', templateName: 'Q3 Revenue Target', status: 'ACTIVE', target: 1000000, currentProgress: 750000 },
            { id: '2', templateName: 'Customer Acquisition', status: 'PENDING', target: 500, currentProgress: 120 },
            { id: '3', templateName: 'Employee Retention', status: 'ACTIVE', target: 95, currentProgress: 98 },
          ]
        });
      })
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <div className="flex h-screen w-full items-center justify-center bg-slate-900">
        <div className="h-12 w-12 animate-spin rounded-full border-4 border-indigo-500 border-t-transparent"></div>
      </div>
    );
  }

  if (!data) return null;

  return (
    <div className="min-h-screen p-8">
      <div className="mx-auto max-w-7xl space-y-8">
        
        {/* Header Section */}
        <header className="glass rounded-2xl p-6 flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
          <div>
            <h1 className="text-3xl font-bold tracking-tight text-white">Welcome back, {data.user.fullName}</h1>
            <div className="mt-2 flex items-center gap-4 text-slate-400">
              <span className="flex items-center gap-1.5"><Building size={16} /> {data.organization.name}</span>
              <span className="flex items-center gap-1.5"><UserIcon size={16} /> {data.user.role}</span>
            </div>
          </div>
          <div className="flex items-center gap-4 bg-slate-900/50 p-4 rounded-xl border border-slate-700">
            <div className="text-right">
              <p className="text-sm text-slate-400 font-medium uppercase tracking-wider">Overall Progress</p>
              <p className="text-3xl font-bold text-indigo-400">{data.overallProgress.toFixed(1)}%</p>
            </div>
            <TrendingUp size={40} className="text-indigo-500 opacity-80" />
          </div>
        </header>

        {/* KPI Grid */}
        <section>
          <h2 className="text-xl font-semibold mb-6 flex items-center gap-2">
            <Target className="text-indigo-400" /> Active Objectives
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {data.kpis.map(kpi => (
              <KpiCard key={kpi.id} kpi={kpi} />
            ))}
          </div>
        </section>

      </div>
    </div>
  );
};

const KpiCard: React.FC<{ kpi: KpiSummary }> = ({ kpi }) => {
  const percent = kpi.target > 0 ? (kpi.currentProgress / kpi.target) * 100 : 0;
  const isPending = kpi.status === 'PENDING' || kpi.status === 'PENDING_MANAGER' || kpi.status === 'PENDING_DIRECTOR';

  return (
    <div className="glass rounded-2xl p-6 transition-all duration-300 hover:-translate-y-1 hover:shadow-indigo-500/10 hover:border-indigo-500/30 group">
      <div className="flex justify-between items-start mb-4">
        <h3 className="font-medium text-lg text-slate-100 group-hover:text-indigo-300 transition-colors">
          {kpi.templateName}
        </h3>
        <span className={clsx(
          "text-xs px-2.5 py-1 rounded-full font-medium flex items-center gap-1.5",
          isPending ? "bg-amber-500/10 text-amber-400 border border-amber-500/20" : "bg-emerald-500/10 text-emerald-400 border border-emerald-500/20"
        )}>
          {isPending ? <Clock size={12} /> : <div className="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse" />}
          {kpi.status}
        </span>
      </div>

      <div className="space-y-3">
        <div className="flex justify-between text-sm text-slate-400">
          <span>Progress</span>
          <span className="font-medium text-slate-200">{kpi.currentProgress.toLocaleString()} / {kpi.target.toLocaleString()}</span>
        </div>
        
        {/* Progress Bar */}
        <div className="h-2 w-full bg-slate-800 rounded-full overflow-hidden">
          <div 
            className="h-full bg-gradient-to-r from-indigo-500 to-indigo-400 transition-all duration-1000 ease-out"
            style={{ width: `${Math.min(percent, 100)}%` }}
          />
        </div>
        <p className="text-xs text-right text-slate-500 font-medium">{percent.toFixed(1)}%</p>
      </div>
    </div>
  );
};
