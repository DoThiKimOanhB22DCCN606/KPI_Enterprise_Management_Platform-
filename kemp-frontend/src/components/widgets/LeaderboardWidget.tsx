import { useQuery } from '@tanstack/react-query';
import { Trophy } from 'lucide-react';
import { analyticsApi } from '../../api/analyticsApi';

interface LeaderboardWidgetProps {
  widget: any;
}

export const LeaderboardWidget = ({ widget }: LeaderboardWidgetProps) => {
  const type = widget.config?.leaderboardType || 'company';
  const period = widget.config?.period || 'Q1';

  const { data, isLoading, isError } = useQuery({
    queryKey: ['leaderboard', type, period],
    queryFn: () => analyticsApi.getLeaderboard(type, period, 5),
  });

  if (isLoading) {
    return (
      <div className="flex flex-col h-full p-4 animate-pulse">
        <div className="h-4 bg-gray-700 rounded w-1/4 mb-4"></div>
        <div className="space-y-3 mt-4">
          {[1, 2, 3].map(i => <div key={i} className="h-8 bg-gray-800 rounded"></div>)}
        </div>
      </div>
    );
  }

  if (isError || !data) {
    return (
      <div className="flex flex-col h-full bg-red-900/10 p-4 justify-center items-center">
        <span className="text-red-400 text-sm">Failed to load Leaderboard Data</span>
      </div>
    );
  }

  if (data.length === 0) {
    return (
      <div className="flex flex-col h-full p-4 justify-center items-center">
        <span className="text-gray-500 text-sm">No leaderboard data available</span>
      </div>
    );
  }

  return (
    <div className="flex flex-col h-full p-4">
      <div className="flex items-center gap-2 text-pink-400 mb-4">
        <Trophy size={18} />
        <h4 className="font-semibold text-sm truncate">{widget.title || 'Top Performers'}</h4>
      </div>
      <div className="flex-1 overflow-auto">
        <div className="space-y-2">
          {data.map((entry, idx) => (
            <div key={entry.entityId} className="flex items-center justify-between p-2 rounded-lg bg-gray-800/50 border border-gray-700/50">
              <div className="flex items-center gap-3 overflow-hidden">
                <span className={`flex items-center justify-center w-6 h-6 rounded-full text-xs font-bold ${idx === 0 ? 'bg-yellow-500/20 text-yellow-500' : idx === 1 ? 'bg-gray-400/20 text-gray-300' : idx === 2 ? 'bg-amber-700/20 text-amber-600' : 'bg-gray-700 text-gray-400'}`}>
                  {entry.rank}
                </span>
                <span className="text-sm text-gray-200 truncate">{entry.entityName}</span>
              </div>
              <span className="text-sm font-semibold text-white ml-2">
                {entry.score.toLocaleString()}
              </span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};
