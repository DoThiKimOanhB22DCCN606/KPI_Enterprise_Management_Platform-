import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { apiClient } from '../lib/apiClient';
import { PageHeader } from '../components/PageHeader';
import { DataTable } from '../components/DataTable';
import type { Column } from '../components/DataTable';
import { KpiStatusBadge } from '../components/KpiStatusBadge';
import { ProgressBar } from '../components/ProgressBar';
import { Plus } from 'lucide-react';

interface Kpi {
  id: string;
  name: string;
  ownerId: string;
  frequency: string;
  target: number;
  currentProgress: number;
  status: string;
  evaluationScore?: number;
}

export default function KpiListPage() {
  const navigate = useNavigate();
  const [page] = useState(0);
  const [statusFilter, setStatusFilter] = useState('ACTIVE');

  const { data, isLoading } = useQuery({
    queryKey: ['kpis', page, statusFilter],
    queryFn: async () => {
      const statusParam = statusFilter ? `&status=${statusFilter}` : '';
      const res = await apiClient.get(`/v1/kpis?page=${page}&size=20${statusParam}`);
      return res.data;
    }
  });

  const columns: Column<Kpi>[] = [
    { header: 'KPI Name', accessor: 'name' },
    { header: 'Owner', accessor: 'ownerId', render: (k) => <span className="text-gray-400 font-mono text-xs">{k.ownerId.substring(0,8)}...</span> },
    { header: 'Frequency', accessor: 'frequency' },
    { header: 'Target', accessor: 'target' },
    { 
      header: 'Progress', 
      render: (k) => {
        const pct = k.target > 0 ? (k.currentProgress / k.target) * 100 : 0;
        return <ProgressBar progress={pct} />;
      } 
    },
    { 
      header: 'Final Score', 
      render: (k) => k.evaluationScore != null ? <span className="text-primary-400 font-bold">{k.evaluationScore}</span> : <span className="text-gray-500">-</span> 
    },
    { header: 'Status', render: (k) => <KpiStatusBadge status={k.status} /> }
  ];

  return (
    <div className="p-8 max-w-7xl mx-auto">
      <PageHeader 
        title="KPI Management" 
        action={
          <div className="flex gap-3">
            <button 
              onClick={() => navigate('/kpis/propose')}
              className="flex items-center gap-2 bg-gray-800 hover:bg-gray-700 border border-gray-700 text-white px-4 py-2 rounded-lg transition"
            >
              Propose Personal KPI
            </button>
            <button 
              onClick={() => navigate('/kpis/new')}
              className="flex items-center gap-2 bg-primary-600 hover:bg-primary-500 text-white px-4 py-2 rounded-lg transition"
            >
              <Plus size={18} /> Create KPI
            </button>
          </div>
        }
      />

      <div className="mb-6 flex gap-4">
        <select 
          className="bg-gray-800 border border-gray-700 text-white rounded-lg px-4 py-2"
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
        >
          <option value="">All Statuses</option>
          <option value="DRAFT">Draft</option>
          <option value="PENDING_MANAGER">Pending</option>
          <option value="ACTIVE">Active</option>
          <option value="CLOSED">Completed</option>
        </select>
      </div>

      <DataTable 
        columns={columns} 
        data={data?.content || []} 
        loading={isLoading}
        onRowClick={(row) => navigate(`/kpis/${row.id}`)}
      />
    </div>
  );
}
