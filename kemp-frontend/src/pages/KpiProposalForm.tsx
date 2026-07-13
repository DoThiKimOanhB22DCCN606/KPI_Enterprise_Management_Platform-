import { useQuery, useMutation } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import toast from 'react-hot-toast';
import { apiClient } from '../lib/apiClient';
import { PageHeader } from '../components/PageHeader';
import { Loader2 } from 'lucide-react';
import { auth } from '../lib/auth';

const proposalSchema = z.object({
  templateId: z.string().min(1, 'Template is required'),
  target: z.number().min(0, 'Target must be at least 0'),
  frequency: z.string().min(1, 'Frequency is required'),
  cycleId: z.string().min(1, 'Evaluation Cycle is required')
});

type ProposalValues = z.infer<typeof proposalSchema>;

export default function KpiProposalForm() {
  const navigate = useNavigate();
  const user = auth.getCurrentUser();

  const { data: templates, isLoading: loadingTemplates } = useQuery({
    queryKey: ['kpi-templates'],
    queryFn: async () => {
      const res = await apiClient.get('/v1/kpi-templates');
      return Array.isArray(res.data) ? res.data : (res.data?.content || []);
    }
  });

  const { data: activeCycles, isLoading: loadingCycles } = useQuery({
    queryKey: ['active-cycles'],
    queryFn: async () => {
      const res = await apiClient.get('/v1/cycles');
      const allCycles = Array.isArray(res.data) ? res.data : (res.data?.content || []);
      return allCycles.filter((c: any) => c.status === 'OPEN' || c.status === 'ACTIVE');
    }
  });

  const { register, handleSubmit, formState: { errors } } = useForm<ProposalValues>({
    resolver: zodResolver(proposalSchema),
    defaultValues: { templateId: '', target: 0, frequency: 'MONTHLY', cycleId: '' }
  });

  const proposeMutation = useMutation({
    mutationFn: async (data: ProposalValues) => {
      // Create KPI
      const kpiRes = await apiClient.post('/v1/kpis', { ...data, ownerId: user.id });
      const kpiId = kpiRes.data.id;
      // Automatically submit for approval
      await apiClient.post(`/v1/kpis/${kpiId}/approvals`, { action: 'SUBMIT' });
      return kpiId;
    },
    onSuccess: (id) => {
      toast.success('Personal KPI proposed successfully!');
      navigate(`/kpis/${id}`);
    },
    onError: (err: any) => {
      toast.error(err.response?.data?.message || 'Failed to propose KPI');
    }
  });

  const onSubmit = (data: ProposalValues) => {
    proposeMutation.mutate(data);
  };

  return (
    <div className="p-8 max-w-3xl mx-auto space-y-8">
      <PageHeader 
        title="Propose Personal KPI"
        breadcrumbs={[{ label: 'KPIs', href: '/kpis' }, { label: 'Propose' }]}
      />

      <div className="glass rounded-xl p-8">
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
          
          <div>
            <label className="block text-sm font-medium text-gray-300 mb-2">KPI Template / Category</label>
            <select 
              {...register('templateId')}
              className={`w-full bg-gray-900 border rounded-lg p-3 text-white outline-none focus:ring-2 focus:ring-primary-500 ${errors.templateId ? 'border-red-500' : 'border-gray-700'}`}
              disabled={loadingTemplates}
            >
              <option value="">-- Select a Template --</option>
              {templates?.map((t: any) => (
                <option key={t.id} value={t.id}>{t.name}</option>
              ))}
            </select>
            {errors.templateId && <p className="text-red-400 text-xs mt-1">{errors.templateId.message}</p>}
          </div>

          <div className="grid grid-cols-2 gap-6">
            <div>
              <label className="block text-sm font-medium text-gray-300 mb-2">Target Value</label>
              <input 
                type="number" 
                step="0.01"
                {...register('target', { valueAsNumber: true })}
                className={`w-full bg-gray-900 border rounded-lg p-3 text-white outline-none focus:ring-2 focus:ring-primary-500 ${errors.target ? 'border-red-500' : 'border-gray-700'}`}
              />
              {errors.target && <p className="text-red-400 text-xs mt-1">{errors.target.message}</p>}
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-300 mb-2">Update Frequency</label>
              <select 
                {...register('frequency')}
                className={`w-full bg-gray-900 border rounded-lg p-3 text-white outline-none focus:ring-2 focus:ring-primary-500 ${errors.frequency ? 'border-red-500' : 'border-gray-700'}`}
              >
                <option value="WEEKLY">Weekly</option>
                <option value="MONTHLY">Monthly</option>
                <option value="QUARTERLY">Quarterly</option>
                <option value="YEARLY">Yearly</option>
              </select>
              {errors.frequency && <p className="text-red-400 text-xs mt-1">{errors.frequency.message}</p>}
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-300 mb-2">Evaluation Cycle</label>
              <select 
                {...register('cycleId')}
                className={`w-full bg-gray-900 border rounded-lg p-3 text-white outline-none focus:ring-2 focus:ring-primary-500 ${errors.cycleId ? 'border-red-500' : 'border-gray-700'}`}
                disabled={loadingCycles}
              >
                <option value="">-- Select Cycle --</option>
                {activeCycles?.map((c: any) => (
                  <option key={c.id} value={c.id}>{c.name} ({new Date(c.periodStart).toLocaleDateString()} - {new Date(c.periodEnd).toLocaleDateString()})</option>
                ))}
              </select>
              {errors.cycleId && <p className="text-red-400 text-xs mt-1">{errors.cycleId.message}</p>}
            </div>
          </div>

          <div className="pt-4 flex justify-end gap-4">
            <button 
              type="button" 
              onClick={() => navigate('/kpis')}
              className="px-6 py-2 text-gray-300 hover:bg-gray-700 rounded-lg transition"
            >
              Cancel
            </button>
            <button 
              type="submit" 
              disabled={proposeMutation.isPending}
              className="flex items-center gap-2 px-6 py-2 bg-primary-600 hover:bg-primary-500 text-white font-medium rounded-lg transition disabled:opacity-50"
            >
              {proposeMutation.isPending && <Loader2 size={18} className="animate-spin" />}
              Submit Proposal
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
