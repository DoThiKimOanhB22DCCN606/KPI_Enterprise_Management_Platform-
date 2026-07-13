import { useEffect, useState } from 'react';
import { apiClient } from '../lib/apiClient';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { Activity, TrendingUp, Users } from 'lucide-react';

interface Kpi {
  id: string;
  name: string;
  currentValue: number;
  targetValue: number;
}

interface LeaderboardEntry {
  entityId: string;
  score: number;
  rank: number;
}

export default function DashboardPage() {
  const [kpis, setKpis] = useState<Kpi[]>([]);
  const [leaderboard, setLeaderboard] = useState<LeaderboardEntry[]>([]);

  useEffect(() => {
    // Fetch KPIs
    apiClient.get('/v1/kpis?size=5&status=ACTIVE')
      .then(res => setKpis(res.data.content || []))
      .catch(err => console.error('Failed to load KPIs', err));

    // Fetch Leaderboard
    apiClient.get('/v1/analytics/leaderboard/employee?period=current-month')
      .then(res => setLeaderboard(res.data))
      .catch(err => console.error('Failed to load leaderboard', err));
  }, []);

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-2xl font-bold text-white tracking-tight">Dashboard Overview</h1>
        <p className="text-gray-400 mt-1">Here's what's happening with your key metrics today.</p>
      </div>

        {/* Top KPIs Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          {kpis.slice(0, 4).map(kpi => {
            const progress = Math.min(100, Math.max(0, (kpi.currentValue / kpi.targetValue) * 100)) || 0;
            return (
              <div key={kpi.id} className="bg-gray-800 rounded-xl p-6 border border-gray-700 shadow-sm">
                <div className="flex justify-between items-start">
                  <div>
                    <p className="text-sm font-medium text-gray-400 truncate max-w-[150px]">{kpi.name}</p>
                    <p className="mt-2 text-2xl font-semibold text-white">{kpi.currentValue}</p>
                  </div>
                  <div className="p-2 bg-primary-900/50 rounded-lg">
                    <Activity className="h-5 w-5 text-primary-500" />
                  </div>
                </div>
                <div className="mt-4 flex items-center justify-between text-sm">
                  <span className="text-gray-400">Target: {kpi.targetValue}</span>
                  <span className={progress >= 100 ? 'text-success' : 'text-primary-500'}>
                    {progress.toFixed(0)}%
                  </span>
                </div>
                <div className="mt-2 w-full bg-gray-900 rounded-full h-1.5 overflow-hidden">
                  <div 
                    className={`h-1.5 rounded-full ${progress >= 100 ? 'bg-success' : 'bg-primary-500'}`}
                    style={{ width: `${progress}%` }}
                  ></div>
                </div>
              </div>
            );
          })}
          
          {/* Fillers if not enough KPIs */}
          {Array.from({ length: Math.max(0, 4 - kpis.length) }).map((_, i) => (
            <div key={`filler-${i}`} className="bg-gray-800 rounded-xl p-6 border border-gray-700 border-dashed flex items-center justify-center opacity-50">
              <span className="text-sm text-gray-500">No data available</span>
            </div>
          ))}
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Chart Section */}
          <div className="lg:col-span-2 bg-gray-800 rounded-xl border border-gray-700 shadow-sm p-6">
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-lg font-semibold text-white flex items-center gap-2">
                <TrendingUp className="h-5 w-5 text-primary-500" />
                Performance Overview
              </h2>
            </div>
            <div className="h-72 w-full">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={kpis} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#374151" vertical={false} />
                  <XAxis dataKey="name" stroke="#9ca3af" fontSize={12} tickLine={false} axisLine={false} />
                  <YAxis stroke="#9ca3af" fontSize={12} tickLine={false} axisLine={false} />
                  <Tooltip 
                    contentStyle={{ backgroundColor: '#1f2937', borderColor: '#374151', borderRadius: '0.5rem', color: '#f3f4f6' }}
                    itemStyle={{ color: '#f3f4f6' }}
                  />
                  <Bar dataKey="currentValue" fill="#3b82f6" radius={[4, 4, 0, 0]} maxBarSize={50} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>

          {/* Leaderboard Section */}
          <div className="bg-gray-800 rounded-xl border border-gray-700 shadow-sm p-6">
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-lg font-semibold text-white flex items-center gap-2">
                <Users className="h-5 w-5 text-warning" />
                Top Performers
              </h2>
              <span className="text-xs bg-gray-700 text-gray-300 px-2 py-1 rounded">This Month</span>
            </div>
            
            <div className="space-y-4">
              {leaderboard.length > 0 ? leaderboard.slice(0, 5).map((entry, idx) => (
                <div key={entry.entityId} className="flex items-center justify-between p-3 rounded-lg bg-gray-900/50 hover:bg-gray-700 transition">
                  <div className="flex items-center gap-3">
                    <div className={`flex items-center justify-center w-8 h-8 rounded-full font-bold text-sm
                      ${idx === 0 ? 'bg-warning/20 text-warning' : 
                        idx === 1 ? 'bg-gray-400/20 text-gray-300' : 
                        idx === 2 ? 'bg-orange-700/20 text-orange-500' : 'bg-gray-800 text-gray-500'}`}>
                      {entry.rank}
                    </div>
                    <div>
                      <p className="text-sm font-medium text-gray-200">{entry.entityId.substring(0, 8)}...</p>
                    </div>
                  </div>
                  <span className="text-sm font-semibold text-primary-400">{entry.score} pts</span>
                </div>
              )) : (
                <div className="text-center py-8 text-gray-500 text-sm">
                  Leaderboard data currently unavailable
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
  );
}
