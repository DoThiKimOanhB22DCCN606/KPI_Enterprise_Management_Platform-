import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '../lib/apiClient';
import { PageHeader } from '../components/PageHeader';
import { DataTable } from '../components/DataTable';
import type { Column } from '../components/DataTable';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { Plus, Play } from 'lucide-react';
import toast from 'react-hot-toast';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';

interface EvaluationCycle {
  id: string;
  name: string;
  type: 'MONTHLY' | 'QUARTERLY' | 'YEARLY';
  periodStart: string;
  periodEnd: string;
  status: 'DRAFT' | 'ACTIVE' | 'OPEN' | 'COMPLETED' | 'CLOSED';
}

const cycleSchema = z.object({
  name: z.string().min(1, 'Name is required'),
  type: z.enum(['MONTHLY', 'QUARTERLY', 'YEARLY']),
  periodStart: z.string().min(1, 'Start date is required'),
  periodEnd: z.string().min(1, 'End date is required')
});

type CycleFormValues = z.infer<typeof cycleSchema>;

export default function CyclesPage() {
  const queryClient = useQueryClient();
  const [isModalOpen, setIsModalOpen] = useState(false);

  const { data: cycles, isLoading } = useQuery<EvaluationCycle[]>({
    queryKey: ['evaluation-cycles'],
    queryFn: async () => (await apiClient.get('/v1/cycles')).data
  });

  const { register, handleSubmit, formState: { errors }, reset } = useForm<CycleFormValues>({
    resolver: zodResolver(cycleSchema),
    defaultValues: {
      name: '',
      type: 'MONTHLY',
      periodStart: new Date().toISOString().split('T')[0],
      periodEnd: new Date(new Date().getFullYear(), new Date().getMonth() + 1, 0).toISOString().split('T')[0]
    }
  });

  const createMutation = useMutation({
    mutationFn: async (data: CycleFormValues) => {
      return await apiClient.post('/v1/cycles', {
        name: data.name,
        type: data.type,
        periodStart: new Date(data.periodStart).toISOString(),
        periodEnd: new Date(data.periodEnd).toISOString()
      });
    },
    onSuccess: () => {
      toast.success('Evaluation cycle created successfully!');
      queryClient.invalidateQueries({ queryKey: ['evaluation-cycles'] });
      setIsModalOpen(false);
      reset();
    },
    onError: (err: any) => {
      toast.error(err.response?.data?.message || 'Failed to create evaluation cycle');
    }
  });

  const activateMutation = useMutation({
    mutationFn: async (id: string) => {
      return await apiClient.post(`/v1/cycles/${id}/activate`);
    },
    onSuccess: () => {
      toast.success('Evaluation cycle activated!');
      queryClient.invalidateQueries({ queryKey: ['evaluation-cycles'] });
    },
    onError: (err: any) => {
      toast.error(err.response?.data?.message || 'Failed to activate cycle');
    }
  });

  const closeMutation = useMutation({
    mutationFn: async (id: string) => {
      return await apiClient.post(`/v1/cycles/${id}/close`);
    },
    onSuccess: () => {
      toast.success('Evaluation cycle closed!');
      queryClient.invalidateQueries({ queryKey: ['evaluation-cycles'] });
    },
    onError: (err: any) => {
      toast.error(err.response?.data?.message || 'Failed to close cycle');
    }
  });

  const columns: Column<EvaluationCycle>[] = [
    { header: 'Cycle Name', accessor: 'name' },
    { header: 'Type', accessor: 'type' },
    { 
      header: 'Period Start', 
      accessor: 'periodStart',
      render: (c) => new Date(c.periodStart).toLocaleDateString()
    },
    { 
      header: 'Period End', 
      accessor: 'periodEnd',
      render: (c) => new Date(c.periodEnd).toLocaleDateString()
    },
    {
      header: 'Status',
      render: (c) => (
        <span className={`px-2.5 py-1 rounded-full text-xs font-semibold border ${
          (c.status === 'ACTIVE' || c.status === 'OPEN')
            ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20' 
            : c.status === 'DRAFT'
            ? 'bg-blue-500/10 text-blue-400 border-blue-500/20'
            : 'bg-gray-500/10 text-gray-400 border-gray-500/20'
        }`}>
          {c.status}
        </span>
      )
    },
    {
      header: 'Actions',
      render: (c) => {
        if (c.status === 'DRAFT') {
          return (
            <button
              onClick={() => activateMutation.mutate(c.id)}
              disabled={activateMutation.isPending}
              className="flex items-center gap-1.5 px-3 py-1.5 bg-emerald-600 hover:bg-emerald-500 text-white rounded-lg text-xs font-medium transition disabled:opacity-50"
            >
              <Play size={12} /> Activate
            </button>
          );
        }
        if (c.status === 'ACTIVE' || c.status === 'OPEN') {
          return (
            <button
              onClick={() => closeMutation.mutate(c.id)}
              disabled={closeMutation.isPending}
              className="flex items-center gap-1.5 px-3 py-1.5 bg-gray-700 hover:bg-gray-600 text-white rounded-lg text-xs font-medium transition disabled:opacity-50"
            >
              Close Cycle
            </button>
          );
        }
        return <span className="text-gray-500 text-xs">-</span>;
      }
    }
  ];

  if (isLoading) return <LoadingSpinner />;

  return (
    <div className="space-y-8">
      <PageHeader 
        title="Evaluation Cycles"
        breadcrumbs={[{ label: 'Cycles' }]}
        action={
          <button 
            onClick={() => setIsModalOpen(true)}
            className="flex items-center gap-2 bg-blue-600 hover:bg-blue-500 text-white px-4 py-2 rounded-lg font-medium transition"
          >
            <Plus size={18} /> Add Evaluation Cycle
          </button>
        }
      />

      <div className="glass rounded-xl overflow-hidden">
        <DataTable data={cycles || []} columns={columns} />
      </div>

      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm">
          <div className="w-full max-w-md bg-gray-800 border border-gray-700 rounded-xl shadow-2xl p-6 overflow-hidden">
            <h3 className="text-lg font-bold text-white mb-4">Create Evaluation Cycle</h3>
            <form onSubmit={handleSubmit((d) => createMutation.mutate(d))} className="space-y-4">
              <div>
                <label className="block text-sm text-gray-300 mb-1">Cycle Name</label>
                <input 
                  type="text" 
                  {...register('name')}
                  className={`w-full bg-gray-900 border rounded-lg p-2.5 text-white outline-none focus:ring-2 focus:ring-blue-500 ${errors.name ? 'border-red-500' : 'border-gray-700'}`}
                  placeholder="e.g. Cycle 2026 Q3"
                />
                {errors.name && <p className="text-red-400 text-xs mt-1">{errors.name.message}</p>}
              </div>

              <div>
                <label className="block text-sm text-gray-300 mb-1">Cycle Type</label>
                <select 
                  {...register('type')}
                  className="w-full bg-gray-900 border border-gray-700 rounded-lg p-2.5 text-white outline-none focus:ring-2 focus:ring-blue-500"
                >
                  <option value="MONTHLY">Monthly</option>
                  <option value="QUARTERLY">Quarterly</option>
                  <option value="YEARLY">Yearly</option>
                </select>
              </div>

              <div>
                <label className="block text-sm text-gray-300 mb-1">Period Start</label>
                <input 
                  type="date" 
                  {...register('periodStart')}
                  className="w-full bg-gray-900 border border-gray-700 rounded-lg p-2.5 text-white outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>

              <div>
                <label className="block text-sm text-gray-300 mb-1">Period End</label>
                <input 
                  type="date" 
                  {...register('periodEnd')}
                  className="w-full bg-gray-900 border border-gray-700 rounded-lg p-2.5 text-white outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>

              <div className="flex justify-end gap-3 mt-6">
                <button 
                  type="button" 
                  onClick={() => setIsModalOpen(false)}
                  className="px-4 py-2 bg-gray-700 hover:bg-gray-600 text-white rounded-lg text-sm font-medium transition"
                >
                  Cancel
                </button>
                <button 
                  type="submit"
                  disabled={createMutation.isPending}
                  className="px-4 py-2 bg-blue-600 hover:bg-blue-500 text-white rounded-lg text-sm font-medium transition disabled:opacity-50"
                >
                  {createMutation.isPending ? 'Creating...' : 'Create'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
