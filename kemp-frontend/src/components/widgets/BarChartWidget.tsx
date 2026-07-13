import { useQuery } from '@tanstack/react-query';
import { BarChart3 } from 'lucide-react';
import { analyticsApi } from '../../api/analyticsApi';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Cell } from 'recharts';

interface BarChartWidgetProps {
  widget: any;
}

export const BarChartWidget = ({ widget }: BarChartWidgetProps) => {
  // Use leaderboard API for the bar chart to show comparison across entities
  const type = widget.config?.leaderboardType || 'company';
  const period = widget.config?.period || 'Q1'; // default test period

  const { data, isLoading, isError } = useQuery({
    queryKey: ['leaderboard-bar', type, period],
    queryFn: () => analyticsApi.getLeaderboard(type, period, 5),
  });

  if (isLoading) {
    return (
      <div className="flex flex-col h-full p-4 animate-pulse">
        <div className="h-4 bg-gray-700 rounded w-1/4 mb-4"></div>
        <div className="flex-1 bg-gray-800 rounded"></div>
      </div>
    );
  }

  if (isError || !data) {
    return (
      <div className="flex flex-col h-full bg-red-900/10 p-4 justify-center items-center">
        <span className="text-red-400 text-sm">Failed to load Bar Chart Data</span>
      </div>
    );
  }

  if (data.length === 0) {
    return (
      <div className="flex flex-col h-full p-4 justify-center items-center">
        <span className="text-gray-500 text-sm">No categorical data available</span>
      </div>
    );
  }

  return (
    <div className="flex flex-col h-full p-4">
      <div className="flex items-center gap-2 text-amber-400 mb-4">
        <BarChart3 size={18} />
        <h4 className="font-semibold text-sm truncate">{widget.title || 'Department Performance'}</h4>
      </div>
      <div className="flex-1 min-h-0">
        <ResponsiveContainer width="100%" height="100%">
          <BarChart data={data} layout="vertical" margin={{ top: 5, right: 20, bottom: 5, left: 10 }}>
            <CartesianGrid strokeDasharray="3 3" stroke="#374151" horizontal={false} />
            <XAxis type="number" stroke="#9CA3AF" fontSize={12} tickLine={false} axisLine={false} />
            <YAxis dataKey="entityName" type="category" stroke="#9CA3AF" fontSize={12} tickLine={false} axisLine={false} width={80} />
            <Tooltip 
              contentStyle={{ backgroundColor: '#1F2937', borderColor: '#374151', color: '#fff' }}
              itemStyle={{ color: '#fff' }}
              cursor={{ fill: '#374151', opacity: 0.4 }}
            />
            <Bar dataKey="score" radius={[0, 4, 4, 0]}>
              {data.map((_entry, index) => (
                <Cell key={`cell-${index}`} fill={index === 0 ? '#FBBF24' : '#FCD34D'} />
              ))}
            </Bar>
          </BarChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
};
