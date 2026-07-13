import React, { useState } from 'react';
import { X, Save } from 'lucide-react';
import { useQuery } from '@tanstack/react-query';
import { apiClient } from '../lib/apiClient';

interface WidgetConfigModalProps {
  widget: any;
  onClose: () => void;
  onSave: (widgetId: string, config: any) => void;
}

export const WidgetConfigModal: React.FC<WidgetConfigModalProps> = ({ widget, onClose, onSave }) => {
  const [kpiId, setKpiId] = useState(widget.config?.kpiId || '');
  const [title, setTitle] = useState(widget.config?.title || '');

  const { data: kpis, isLoading: isLoadingKpis } = useQuery({
    queryKey: ['kpis-list'],
    queryFn: async () => {
      const res = await apiClient.get('/v1/kpis?page=0&size=100');
      // The API might return { data: { content: [...] } } depending on Spring Data Page wrapper
      return res.data?.content || res.data || [];
    }
  });

  const handleSave = () => {
    onSave(widget.i, { kpiId, title });
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm">
      <div className="bg-gray-800 rounded-xl max-w-md w-full border border-gray-700 p-6 relative shadow-2xl">
        <button 
          onClick={onClose}
          className="absolute top-4 right-4 text-gray-400 hover:text-white transition"
        >
          <X size={20} />
        </button>

        <h3 className="text-xl font-bold text-white mb-6">Configure Widget</h3>
        
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-300 mb-1">Widget Title</label>
            <input 
              type="text" 
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              className="w-full bg-gray-900 border border-gray-700 rounded-lg p-2.5 text-white outline-none focus:ring-2 focus:ring-primary-500"
              placeholder="e.g. Sales Performance"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-300 mb-1">Data Source (KPI)</label>
            <select 
              value={kpiId}
              onChange={(e) => setKpiId(e.target.value)}
              className="w-full bg-gray-900 border border-gray-700 rounded-lg p-2.5 text-white outline-none focus:ring-2 focus:ring-primary-500"
              disabled={isLoadingKpis}
            >
              <option value="">Select a KPI...</option>
              {Array.isArray(kpis) && kpis.map((kpi: any) => (
                <option key={kpi.id} value={kpi.id}>
                  {kpi.name || kpi.id.substring(0, 8)}
                </option>
              ))}
            </select>
          </div>
        </div>

        <div className="flex justify-end gap-3 mt-8">
          <button 
            onClick={onClose} 
            className="px-4 py-2 text-gray-300 hover:bg-gray-700 rounded-lg font-medium transition"
          >
            Cancel
          </button>
          <button 
            onClick={handleSave} 
            className="flex items-center gap-2 px-4 py-2 bg-primary-600 hover:bg-primary-500 text-white rounded-lg font-medium transition"
          >
            <Save size={16} /> Save Configuration
          </button>
        </div>
      </div>
    </div>
  );
};
