import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { apiClient } from '../lib/apiClient';
import { PageHeader } from '../components/PageHeader';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { Trophy, TrendingUp, TrendingDown, Minus } from 'lucide-react';
import clsx from 'clsx';

interface LeaderboardEntry {
  entityId: string;
  name?: string;
  score: number;
  rank: number;
  previousRank?: number;
}

export default function LeaderboardPage() {
  const [type, setType] = useState('employee');
  const [period, setPeriod] = useState('current-month');

  const { data, isLoading } = useQuery({
    queryKey: ['leaderboard', type, period],
    queryFn: async () => (await apiClient.get(`/v1/analytics/leaderboard/${type}?period=${period}`)).data as LeaderboardEntry[],
    refetchInterval: 30000 // Auto-refresh every 30 seconds
  });

  return (
    <div className="p-8 max-w-5xl mx-auto">
      <PageHeader title="Leaderboard" />

      <div className="flex flex-col md:flex-row gap-4 mb-8 justify-between">
        <div className="flex bg-gray-800 p-1 rounded-lg border border-gray-700">
          {['employee', 'store', 'region'].map(t => (
            <button
              key={t}
              onClick={() => setType(t)}
              className={clsx(
                "px-4 py-2 text-sm font-medium rounded-md capitalize transition",
                type === t ? "bg-primary-600 text-white" : "text-gray-400 hover:text-white hover:bg-gray-700"
              )}
            >
              {t}
            </button>
          ))}
        </div>
        
        <select 
          value={period}
          onChange={(e) => setPeriod(e.target.value)}
          className="bg-gray-800 border border-gray-700 text-white rounded-lg px-4 py-2 text-sm"
        >
          <option value="current-month">This Month</option>
          <option value="last-month">Last Month</option>
          <option value="current-quarter">This Quarter</option>
          <option value="current-year">This Year</option>
        </select>
      </div>

      <div className="bg-gray-800 border border-gray-700 rounded-xl overflow-hidden shadow">
        {isLoading ? <LoadingSpinner /> : (
          <div className="divide-y divide-gray-700">
            {data?.map((entry) => {
              const isTop3 = entry.rank <= 3;
              return (
                <div key={entry.entityId} className="flex items-center p-4 hover:bg-gray-700/50 transition">
                  <div className="w-16 flex justify-center">
                    {isTop3 ? (
                      <Trophy className={clsx(
                        "h-6 w-6",
                        entry.rank === 1 && "text-yellow-400",
                        entry.rank === 2 && "text-gray-300",
                        entry.rank === 3 && "text-amber-600"
                      )} />
                    ) : (
                      <span className="text-gray-500 font-mono text-lg">{entry.rank}</span>
                    )}
                  </div>
                  
                  <div className="flex-1">
                    <div className="font-bold text-white text-lg">{entry.name || `ID: ${entry.entityId.substring(0,8)}`}</div>
                  </div>
                  
                  <div className="w-32 text-right">
                    <div className="text-xl font-bold text-primary-400">{entry.score} pts</div>
                  </div>
                  
                  <div className="w-16 flex justify-center">
                    {entry.previousRank ? (
                      entry.previousRank > entry.rank ? <TrendingUp className="text-green-500" /> :
                      entry.previousRank < entry.rank ? <TrendingDown className="text-red-500" /> :
                      <Minus className="text-gray-500" />
                    ) : <Minus className="text-gray-500" />}
                  </div>
                </div>
              );
            })}
            
            {(!data || data.length === 0) && (
              <div className="p-8 text-center text-gray-500">No data available for this period.</div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
