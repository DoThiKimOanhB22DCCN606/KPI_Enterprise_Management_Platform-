import { useQuery } from '@tanstack/react-query';
import { LineChart as LucideLineChart } from 'lucide-react';
import { analyticsApi } from '../../api/analyticsApi';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend } from 'recharts';

interface LineChartWidgetProps {
  widget: any;
}

export const LineChartWidget = ({ widget }: LineChartWidgetProps) => {
  const kpiId = widget.kpiId || '00000000-0000-0000-0000-000000000001';

  const { data, isLoading, isError } = useQuery({
    queryKey: ['kpi-trend', kpiId],
    queryFn: () => analyticsApi.getKpiTrend(kpiId),
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
        <span className="text-red-400 text-sm">Failed to load Chart Data</span>
      </div>
    );
  }

  if (data.length === 0) {
    return (
      <div className="flex flex-col h-full p-4 justify-center items-center">
        <span className="text-gray-500 text-sm">No trend data available</span>
      </div>
    );
  }

  return (
    <div className="flex flex-col h-full p-4">
      <div className="flex items-center gap-2 text-emerald-400 mb-4">
        <LucideLineChart size={18} />
        <h4 className="font-semibold text-sm truncate">{widget.title || 'Growth Trend'}</h4>
      </div>
      <div className="flex-1 min-h-0">
        <ResponsiveContainer width="100%" height="100%">
          <LineChart data={data} margin={{ top: 5, right: 20, bottom: 5, left: 0 }}>
            <CartesianGrid strokeDasharray="3 3" stroke="#374151" vertical={false} />
            <XAxis dataKey="period" stroke="#9CA3AF" fontSize={12} tickLine={false} axisLine={false} />
            <YAxis stroke="#9CA3AF" fontSize={12} tickLine={false} axisLine={false} />
            <Tooltip 
              contentStyle={{ backgroundColor: '#1F2937', borderColor: '#374151', color: '#fff' }}
              itemStyle={{ color: '#fff' }}
            />
            <Legend wrapperStyle={{ fontSize: '12px' }} />
            <Line type="monotone" dataKey="value" stroke="#34D399" strokeWidth={3} dot={{ r: 4, fill: '#34D399' }} activeDot={{ r: 6 }} name="Actual" />
            <Line type="monotone" dataKey="target" stroke="#9CA3AF" strokeWidth={2} strokeDasharray="5 5" dot={false} name="Target" />
          </LineChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
};
