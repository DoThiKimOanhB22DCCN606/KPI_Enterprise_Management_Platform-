import { useQuery } from '@tanstack/react-query';
import { Gauge } from 'lucide-react';
import { analyticsApi } from '../../api/analyticsApi';
import { PieChart, Pie, Cell, ResponsiveContainer } from 'recharts';

interface GaugeWidgetProps {
  widget: any;
}

export const GaugeWidget = ({ widget }: GaugeWidgetProps) => {
  const kpiId = widget.kpiId || '00000000-0000-0000-0000-000000000001';

  const { data, isLoading, isError } = useQuery({
    queryKey: ['kpi-gauge', kpiId],
    queryFn: () => analyticsApi.getKpiPerformance(kpiId),
  });

  if (isLoading) {
    return (
      <div className="flex flex-col h-full p-4 animate-pulse">
        <div className="h-4 bg-gray-700 rounded w-1/4 mb-4"></div>
        <div className="flex-1 rounded-full bg-gray-800 mx-10 my-4"></div>
      </div>
    );
  }

  if (isError || !data) {
    return (
      <div className="flex flex-col h-full bg-red-900/10 p-4 justify-center items-center">
        <span className="text-red-400 text-sm">Failed to load Gauge Data</span>
      </div>
    );
  }

  const performance = Math.min(Math.max(data.performancePct, 0), 100);
  const pieData = [
    { name: 'Achieved', value: performance },
    { name: 'Remaining', value: 100 - performance }
  ];
  
  const COLORS = ['#A78BFA', '#374151']; // Purple and Gray

  return (
    <div className="flex flex-col h-full p-4">
      <div className="flex items-center gap-2 text-purple-400 mb-2">
        <Gauge size={18} />
        <h4 className="font-semibold text-sm truncate">{widget.title || 'System Health'}</h4>
      </div>
      <div className="flex-1 min-h-0 relative">
        <ResponsiveContainer width="100%" height="100%">
          <PieChart>
            <Pie
              data={pieData}
              cx="50%"
              cy="70%"
              startAngle={180}
              endAngle={0}
              innerRadius="60%"
              outerRadius="80%"
              paddingAngle={2}
              dataKey="value"
              stroke="none"
            >
              {pieData.map((_entry, index) => (
                <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
              ))}
            </Pie>
          </PieChart>
        </ResponsiveContainer>
        <div className="absolute bottom-4 left-0 right-0 text-center">
          <span className="text-2xl font-bold text-white">{performance.toFixed(1)}%</span>
        </div>
      </div>
    </div>
  );
};
