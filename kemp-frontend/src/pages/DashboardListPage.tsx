import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { apiClient } from '../lib/apiClient';
import { PageHeader } from '../components/PageHeader';
import { Plus, Eye, LayoutDashboard, Calendar } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

interface DashboardItem {
  id: string;
  name: string;
  createdAt: string;
  updatedAt: string;
}

export default function DashboardListPage() {
  const navigate = useNavigate();
  const [page, setPage] = useState(0);

  const { data, isLoading, isError } = useQuery({
    queryKey: ['dashboards', page],
    queryFn: async () => {
      const res = await apiClient.get(`/v1/dashboards?page=${page}&size=12`);
      return res.data;
    }
  });

  return (
    <div className="p-4 md:p-8 max-w-[1600px] mx-auto min-h-screen flex flex-col">
      <PageHeader 
        title="Dashboards" 
        action={
          <button 
            onClick={() => navigate('/dashboard/builder')}
            className="flex items-center gap-2 bg-primary-600 hover:bg-primary-500 text-white px-4 py-2 rounded-lg transition"
          >
            <Plus size={16} /> Create New
          </button>
        }
      />

      <div className="mt-6 flex-1">
        {isLoading ? (
          <div className="flex justify-center items-center h-64">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-500"></div>
          </div>
        ) : isError ? (
          <div className="text-center text-red-400 py-12 bg-gray-800 rounded-xl border border-gray-700">
            Failed to load dashboards. Please try again.
          </div>
        ) : data?.content?.length === 0 ? (
          <div className="text-center py-20 bg-gray-800/50 rounded-xl border border-gray-700 border-dashed flex flex-col items-center">
            <LayoutDashboard className="h-12 w-12 text-gray-500 mb-4" />
            <h3 className="text-lg font-medium text-white mb-2">No dashboards found</h3>
            <p className="text-gray-400 mb-6">Get started by creating your first custom dashboard.</p>
            <button 
              onClick={() => navigate('/dashboard/builder')}
              className="inline-flex items-center gap-2 bg-primary-600 hover:bg-primary-500 text-white px-4 py-2 rounded-lg transition"
            >
              <Plus size={16} /> Create Dashboard
            </button>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
            {data?.content?.map((dashboard: DashboardItem) => (
              <div key={dashboard.id} className="bg-gray-800 rounded-xl border border-gray-700 hover:border-primary-500/50 transition shadow-sm overflow-hidden flex flex-col">
                <div className="p-6 flex-1">
                  <div className="flex justify-between items-start mb-4">
                    <div className="bg-gray-700/50 p-3 rounded-lg text-primary-400">
                      <LayoutDashboard size={24} />
                    </div>
                  </div>
                  <h3 className="text-xl font-bold text-white mb-2 truncate" title={dashboard.name}>
                    {dashboard.name}
                  </h3>
                  <div className="flex items-center gap-2 text-sm text-gray-400 mt-4">
                    <Calendar size={14} />
                    <span>Created: {dashboard.createdAt ? new Date(dashboard.createdAt).toLocaleDateString() : 'Unknown'}</span>
                  </div>
                </div>
                <div className="bg-gray-900/50 px-6 py-4 border-t border-gray-700 flex justify-end gap-3">
                  <button 
                    onClick={() => navigate(`/dashboards/view/${dashboard.id}`)}
                    className="flex items-center gap-2 text-sm px-4 py-2 rounded-lg bg-gray-800 text-gray-300 hover:text-white hover:bg-gray-700 border border-gray-600 hover:border-gray-500 transition w-full justify-center"
                  >
                    <Eye size={16} /> View Dashboard
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
        
        {data?.totalPages > 1 && (
          <div className="mt-8 flex justify-center gap-2">
            <button 
              onClick={() => setPage(old => Math.max(0, old - 1))}
              disabled={page === 0}
              className="px-4 py-2 bg-gray-800 border border-gray-700 rounded-lg text-white disabled:opacity-50 hover:bg-gray-700 transition"
            >
              Previous
            </button>
            <span className="flex items-center px-4 text-gray-400 font-medium">
              Page {page + 1} of {data.totalPages}
            </span>
            <button 
              onClick={() => setPage(old => (old < data.totalPages - 1 ? old + 1 : old))}
              disabled={page >= data.totalPages - 1}
              className="px-4 py-2 bg-gray-800 border border-gray-700 rounded-lg text-white disabled:opacity-50 hover:bg-gray-700 transition"
            >
              Next
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
