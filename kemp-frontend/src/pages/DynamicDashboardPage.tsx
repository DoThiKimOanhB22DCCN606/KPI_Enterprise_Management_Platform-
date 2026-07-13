import { useParams, useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { Responsive, WidthProvider } from 'react-grid-layout/legacy';
import 'react-grid-layout/css/styles.css';
import 'react-resizable/css/styles.css';
import { PageHeader } from '../components/PageHeader';
import { apiClient } from '../lib/apiClient';
import { ArrowLeft, LayoutGrid } from 'lucide-react';

const ResponsiveGridLayout = WidthProvider(Responsive);

import { KpiCardWidget } from '../components/widgets/KpiCardWidget';
import { LineChartWidget } from '../components/widgets/LineChartWidget';
import { BarChartWidget } from '../components/widgets/BarChartWidget';
import { GaugeWidget } from '../components/widgets/GaugeWidget';
import { LeaderboardWidget } from '../components/widgets/LeaderboardWidget';

// Component mapping based on widget type
const WidgetRenderer = ({ widget }: { widget: any }) => {
  switch (widget.type) {
    case 'KPI_CARD':
      return <KpiCardWidget widget={widget} />;
    case 'LINE_CHART':
      return <LineChartWidget widget={widget} />;
    case 'BAR_CHART':
      return <BarChartWidget widget={widget} />;
    case 'GAUGE':
      return <GaugeWidget widget={widget} />;
    case 'LEADERBOARD':
      return <LeaderboardWidget widget={widget} />;
    default:
      return (
        <div className="flex flex-col h-full p-4 items-center justify-center">
          <span className="text-gray-500">Unknown Widget: {widget.type}</span>
        </div>
      );
  }
};

export default function DynamicDashboardPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const { data: dashboard, isLoading, isError } = useQuery({
    queryKey: ['dashboard', id],
    queryFn: async () => {
      const res = await apiClient.get(`/v1/dashboards/${id}`);
      return res.data;
    },
    enabled: !!id
  });

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-950">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-primary-500"></div>
      </div>
    );
  }

  if (isError || !dashboard) {
    return (
      <div className="min-h-screen p-8 bg-gray-950">
        <div className="bg-red-900/20 border border-red-500/50 text-red-400 p-4 rounded-xl flex items-center justify-center">
          Failed to load dashboard. It might not exist or you don't have access.
        </div>
        <button 
          onClick={() => navigate('/dashboards')}
          className="mt-4 flex items-center gap-2 text-primary-400 hover:text-primary-300 transition"
        >
          <ArrowLeft size={16} /> Back to Dashboards
        </button>
      </div>
    );
  }

  let layoutData: any = { layouts: { lg: [] }, widgets: [] };
  try {
    if (dashboard.layoutJson) {
      layoutData = JSON.parse(dashboard.layoutJson);
    }
  } catch (e) {
    console.error('Failed to parse layoutJson', e);
  }

  const { layouts, widgets } = layoutData;

  return (
    <div className="p-4 md:p-8 max-w-[1600px] mx-auto min-h-screen flex flex-col">
      <PageHeader 
        title={dashboard.name || 'Dashboard View'}
        action={
          <button 
            onClick={() => navigate('/dashboards')}
            className="flex items-center gap-2 bg-gray-800 hover:bg-gray-700 text-white px-4 py-2 rounded-lg transition border border-gray-700"
          >
            <ArrowLeft size={16} /> Back
          </button>
        }
      />

      <div className="flex-1 mt-6 bg-gray-900/50 rounded-xl border border-gray-800 overflow-hidden relative min-h-[600px]">
        {(!widgets || widgets.length === 0) ? (
          <div className="absolute inset-0 flex flex-col items-center justify-center text-gray-500">
            <LayoutGrid className="opacity-20 mb-4" size={48} />
            <p>This dashboard has no widgets configured.</p>
          </div>
        ) : (
          <div className="p-4">
            <ResponsiveGridLayout
              className="layout"
              layouts={layouts}
              breakpoints={{ lg: 1200, md: 996, sm: 768, xs: 480, xxs: 0 }}
              cols={{ lg: 12, md: 10, sm: 6, xs: 4, xxs: 2 }}
              rowHeight={100}
              isDraggable={false}
              isResizable={false}
              isDroppable={false}
              useCSSTransforms={true}
            >
              {(widgets || []).map((w: any) => (
                <div 
                  key={w.i} 
                  className="bg-gray-800 rounded-xl border border-gray-700/60 shadow-lg overflow-hidden transition-all hover:border-gray-600 group"
                >
                  <WidgetRenderer widget={w} />
                </div>
              ))}
            </ResponsiveGridLayout>
          </div>
        )}
      </div>
    </div>
  );
}


