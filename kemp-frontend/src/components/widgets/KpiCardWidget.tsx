import { useQuery } from '@tanstack/react-query';
import { Activity } from 'lucide-react';
import { analyticsApi } from '../../api/analyticsApi';

interface KpiCardWidgetProps {
  widget: any;
}

export const KpiCardWidget = ({ widget }: KpiCardWidgetProps) => {
  // Use widget.kpiId if available, otherwise use a fallback test UUID
  const kpiId = widget.kpiId || '00000000-0000-0000-0000-000000000001';

  const { data, isLoading, isError } = useQuery({
    queryKey: ['kpi-performance', kpiId],
    queryFn: () => analyticsApi.getKpiPerformance(kpiId),
  });

  if (isLoading) {
    return (
      <div className="flex flex-col h-full bg-gradient-to-br from-blue-900/40 to-blue-800/20 p-4 animate-pulse">
        <div className="h-4 bg-gray-700 rounded w-1/3 mb-4"></div>
        <div className="h-8 bg-gray-700 rounded w-1/2 mt-auto"></div>
      </div>
    );
  }

  if (isError || !data) {
    return (
      <div className="flex flex-col h-full bg-red-900/10 p-4 justify-center items-center">
        <span className="text-red-400 text-sm text-center">Failed to load KPI Data</span>
      </div>
    );
  }

  // Calculate variance text
  const variance = data.performancePct - 100;
  const isPositive = variance >= 0;

  return (
    <div className="flex flex-col h-full bg-gradient-to-br from-blue-900/40 to-blue-800/20 p-4">
      <div className="flex items-center gap-2 text-blue-400 mb-2">
        <Activity size={18} />
        <h4 className="font-semibold text-sm truncate">{widget.title || 'KPI Performance'}</h4>
      </div>
      <p className="text-3xl font-bold text-white mt-auto">
        {data.currentValue.toLocaleString(undefined, { maximumFractionDigits: 1 })}
      </p>
      <p className={`text-xs mt-1 ${isPositive ? 'text-emerald-400' : 'text-rose-400'}`}>
        {isPositive ? '+' : ''}{variance.toFixed(1)}% vs target ({data.targetValue})
      </p>
    </div>
  );
};
