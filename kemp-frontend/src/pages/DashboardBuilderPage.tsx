import { useState } from 'react';
import { Responsive, WidthProvider } from 'react-grid-layout/legacy';
import 'react-grid-layout/css/styles.css';
import 'react-resizable/css/styles.css';
import { PageHeader } from '../components/PageHeader';
import { Save, Share2, Eye, LayoutGrid, X, Settings } from 'lucide-react';
import { useMutation } from '@tanstack/react-query';
import { apiClient } from '../lib/apiClient';
import toast from 'react-hot-toast';
import { useNavigate } from 'react-router-dom';
import { WidgetConfigModal } from '../components/WidgetConfigModal';

const ResponsiveGridLayout = WidthProvider(Responsive);

export default function DashboardBuilderPage() {
  const navigate = useNavigate();
  const [layouts, setLayouts] = useState<any>({ lg: [] });
  const [widgets, setWidgets] = useState<any[]>([]);
  const [showSaveModal, setShowSaveModal] = useState(false);
  const [dashboardName, setDashboardName] = useState('');
  const [configuringWidget, setConfiguringWidget] = useState<any>(null);
  const [isPreviewMode, setIsPreviewMode] = useState(false);

  const saveMutation = useMutation({
    mutationFn: async (data: any) => {
      return await apiClient.post('/v1/dashboards', data);
    },
    onSuccess: () => {
      toast.success('Dashboard saved successfully!');
      setShowSaveModal(false);
      navigate('/dashboards'); // assuming this route exists or they stay here
    },
    onError: (err: any) => {
      toast.error(err.response?.data?.message || 'Failed to save dashboard');
    }
  });

  const onLayoutChange = (_layout: any, allLayouts: any) => {
    setLayouts(allLayouts);
  };

  const onDrop = (_layout: any, layoutItem: any, _event: any) => {
    // Extract widget type from the drag event dataTransfer if possible,
    // but react-grid-layout handles data differently. We can read the global dataTransfer.
    const widgetType = _event.dataTransfer?.getData('text/plain') || 'WIDGET';
    const newWidget = {
      i: `widget-${Date.now()}`,
      x: layoutItem?.x || 0,
      y: layoutItem?.y || 0,
      w: 4,
      h: 2,
      type: widgetType
    };

    setWidgets([...widgets, newWidget]);
  };

  const handleSave = () => {
    if (!dashboardName.trim()) {
      toast.error('Dashboard name is required');
      return;
    }
    
    // Combine widgets and layouts into a single layoutJson for the backend
    const layoutJson = JSON.stringify({
      layouts,
      widgets
    });

    saveMutation.mutate({
      name: dashboardName,
      layoutJson
    });
  };

  return (
    <div className="p-4 md:p-8 max-w-[1600px] mx-auto min-h-screen flex flex-col">
      <PageHeader 
        title="Dashboard Builder" 
        action={
          <div className="flex gap-3">
            <button 
              onClick={() => setIsPreviewMode(!isPreviewMode)}
              className={`flex items-center gap-2 px-4 py-2 rounded-lg transition border ${isPreviewMode ? 'bg-primary-600 border-primary-500 text-white' : 'bg-gray-800 hover:bg-gray-700 border-gray-700 text-white'}`}
            >
              <Eye size={16} /> {isPreviewMode ? 'Exit Preview' : 'Preview'}
            </button>
            <button 
              onClick={() => toast.success('Dashboard link copied to clipboard!')}
              className="flex items-center gap-2 bg-gray-800 hover:bg-gray-700 text-white px-4 py-2 rounded-lg transition border border-gray-700"
            >
              <Share2 size={16} /> Share
            </button>
            <button 
              onClick={() => setShowSaveModal(true)}
              className="flex items-center gap-2 bg-primary-600 hover:bg-primary-500 text-white px-4 py-2 rounded-lg transition"
            >
              <Save size={16} /> Save
            </button>
          </div>
        }
      />

      <div className="flex gap-6 flex-1 min-h-[600px] mt-4">
        {/* Widget Palette Sidebar */}
        {!isPreviewMode && (
        <div className="w-64 bg-gray-800 rounded-xl border border-gray-700 p-4 hidden lg:block">
          <h3 className="font-bold text-white mb-4 flex items-center gap-2"><LayoutGrid size={18}/> Widgets</h3>
          <div className="space-y-3">
            {['LINE_CHART', 'BAR_CHART', 'GAUGE', 'KPI_CARD', 'LEADERBOARD'].map(type => (
              <div 
                key={type} 
                className="bg-gray-900 border border-gray-700 p-3 rounded-lg cursor-grab hover:border-primary-500 transition text-sm font-medium text-gray-300 flex items-center justify-center h-16"
                draggable
                unselectable="on"
                onDragStart={(e) => {
                  e.dataTransfer.setData('text/plain', type);
                }}
              >
                {type.replace('_', ' ')}
              </div>
            ))}
          </div>
        </div>
        )}

        {/* Canvas Area */}
        <div className={`flex-1 rounded-xl overflow-hidden relative ${isPreviewMode ? 'bg-gray-900 border border-gray-800' : 'bg-gray-900/50 border border-gray-700 border-dashed'}`}>
          <div className="absolute inset-0 overflow-auto p-4">
            {widgets.length === 0 && (
              <div className="absolute inset-0 flex items-center justify-center pointer-events-none text-gray-500">
                Drag and drop widgets here
              </div>
            )}
            <ResponsiveGridLayout
              className="layout min-h-full"
              layouts={layouts}
              breakpoints={{ lg: 1200, md: 996, sm: 768, xs: 480, xxs: 0 }}
              cols={{ lg: 12, md: 10, sm: 6, xs: 4, xxs: 2 }}
              rowHeight={100}
              onLayoutChange={onLayoutChange}
              isDroppable={!isPreviewMode}
              onDrop={onDrop}
              isDraggable={!isPreviewMode}
              isResizable={!isPreviewMode}
            >
              {widgets.map(w => (
                <div key={w.i} data-grid={{ x: w.x, y: w.y, w: w.w, h: w.h }} className={`bg-gray-800 rounded-xl shadow flex items-center justify-center text-gray-300 relative group ${!isPreviewMode ? 'border border-gray-700 hover:border-primary-500 cursor-pointer' : 'border border-gray-700/50'}`}>
                  <div className="flex flex-col items-center">
                    <span className="font-semibold">{w.type.replace('_', ' ')}</span>
                    {w.config?.title && <span className="text-xs text-gray-400 mt-1">{w.config.title}</span>}
                  </div>
                  {!isPreviewMode && (
                    <>
                  <button 
                    onClick={() => setConfiguringWidget(w)}
                    className="absolute top-2 right-8 text-gray-500 hover:text-blue-400 opacity-0 group-hover:opacity-100 transition"
                  >
                    <Settings size={16} />
                  </button>
                  <button 
                    onClick={() => {
                      setWidgets(widgets.filter(ww => ww.i !== w.i));
                    }}
                    className="absolute top-2 right-2 text-gray-500 hover:text-red-400 opacity-0 group-hover:opacity-100 transition"
                  >
                    <X size={16} />
                  </button>
                  </>
                  )}
                </div>
              ))}
            </ResponsiveGridLayout>
          </div>
        </div>
      </div>

      {showSaveModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm">
          <div className="bg-gray-800 rounded-xl max-w-md w-full border border-gray-700 p-6">
            <h3 className="text-xl font-bold text-white mb-4">Save Dashboard</h3>
            <div className="mb-6">
              <label className="block text-sm text-gray-400 mb-2">Dashboard Name *</label>
              <input 
                type="text" 
                value={dashboardName}
                onChange={(e) => setDashboardName(e.target.value)}
                className="w-full bg-gray-900 border border-gray-700 rounded-lg p-3 text-white outline-none focus:ring-2 focus:ring-primary-500" 
                placeholder="e.g. Q3 Sales Overview"
                autoFocus
              />
            </div>
            <div className="flex justify-end gap-3">
              <button 
                onClick={() => setShowSaveModal(false)} 
                className="px-4 py-2 text-gray-300 hover:bg-gray-700 rounded-lg"
              >
                Cancel
              </button>
              <button 
                onClick={handleSave} 
                disabled={saveMutation.isPending || !dashboardName.trim()}
                className="px-4 py-2 bg-primary-600 hover:bg-primary-500 text-white rounded-lg disabled:opacity-50"
              >
                {saveMutation.isPending ? 'Saving...' : 'Save Layout'}
              </button>
            </div>
          </div>
        </div>
      )}

      {configuringWidget && (
        <WidgetConfigModal 
          widget={configuringWidget}
          onClose={() => setConfiguringWidget(null)}
          onSave={(id, config) => {
            setWidgets(widgets.map(w => w.i === id ? { ...w, config } : w));
            setConfiguringWidget(null);
          }}
        />
      )}
    </div>
  );
}
