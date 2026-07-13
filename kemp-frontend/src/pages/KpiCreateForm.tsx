import { useQuery, useMutation } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import toast from 'react-hot-toast';
import { apiClient } from '../lib/apiClient';
import { PageHeader } from '../components/PageHeader';
import { Loader2 } from 'lucide-react';

const createKpiSchema = z.object({
  templateId: z.string().min(1, 'Template is required'),
  target: z.number().min(0, 'Target must be at least 0'),
  frequency: z.string().min(1, 'Frequency is required'),
  assignType: z.enum(['USER', 'DEPARTMENT']),
  ownerId: z.string().min(1, 'Selection is required'),
  cycleId: z.string().min(1, 'Evaluation Cycle is required')
});

type CreateKpiValues = z.infer<typeof createKpiSchema>;

export default function KpiCreateForm() {
  const navigate = useNavigate();

  const { register, handleSubmit, formState: { errors }, watch, setValue } = useForm<CreateKpiValues>({
    resolver: zodResolver(createKpiSchema),
    defaultValues: { templateId: '', ownerId: '', target: 0, frequency: 'MONTHLY', assignType: 'USER', cycleId: '' }
  });

  const { data: templates, isLoading: loadingTemplates } = useQuery({
    queryKey: ['kpi-templates'],
    queryFn: async () => {
      const res = await apiClient.get('/v1/kpi-templates');
      return Array.isArray(res.data) ? res.data : (res.data?.content || []);
    }
  });

  const { data: users, isLoading: loadingUsers } = useQuery({
    queryKey: ['users'],
    queryFn: async () => {
      const res = await apiClient.get('/v1/users');
      return Array.isArray(res.data) ? res.data : (res.data?.content || []);
    }
  });

  const { data: orgUnits, isLoading: loadingOrgUnits } = useQuery({
    queryKey: ['org-units'],
    queryFn: async () => {
      const res = await apiClient.get('/v1/org-units?size=100');
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


  const assignType = watch('assignType');

  const createMutation = useMutation({
    mutationFn: async (data: CreateKpiValues) => {
      return await apiClient.post('/v1/kpis', data);
    },
    onSuccess: () => {
      toast.success('KPI Created successfully!');
      navigate('/kpis');
    },
    onError: (err: any) => {
      toast.error(err.response?.data?.message || 'Failed to create KPI');
    }
  });

  const onSubmit = async (data: CreateKpiValues) => {
    if (data.assignType === 'USER') {
      createMutation.mutate({ ...data, ownerId: data.ownerId });
    } else {
      // Bulk assignment to department
      if (!users) return;
      const deptUsers = users.filter((u: any) => u.organizationUnitId === data.ownerId);
      if (deptUsers.length === 0) {
        toast.error('No users found in this department');
        return;
      }
      
      let successCount = 0;
      toast.loading('Assigning to department...', { id: 'bulk' });
      for (const u of deptUsers) {
        try {
          await apiClient.post('/v1/kpis', { ...data, ownerId: u.id });
          successCount++;
        } catch (e) {
          console.error('Failed for user', u.id, e);
        }
      }
      toast.success(`KPI Assigned to ${successCount} users in department`, { id: 'bulk' });
      navigate('/kpis');
    }
  };

  return (
    <div className="p-8 max-w-3xl mx-auto space-y-8">
      <PageHeader 
        title="Create New KPI"
        breadcrumbs={[{ label: 'KPIs', href: '/kpis' }, { label: 'New KPI' }]}
      />

      <div className="glass rounded-xl p-8">
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
          
          <div>
            <label className="block text-sm font-medium text-gray-300 mb-2">KPI Template / Name</label>
            <select 
              {...register('templateId')}
              className={`w-full bg-gray-900 border rounded-lg p-3 text-white outline-none focus:ring-2 focus:ring-primary-500 ${errors.templateId ? 'border-red-500' : 'border-gray-700'}`}
              disabled={loadingTemplates}
            >
              <option value="">-- Select a KPI Template --</option>
              {templates?.map((t: any) => (
                <option key={t.id} value={t.id}>{t.name}</option>
              ))}
            </select>
            {errors.templateId && <p className="text-red-400 text-xs mt-1">{errors.templateId.message}</p>}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-300 mb-2">Assign To</label>
            <div className="flex gap-4 mb-4">
              <label className="flex items-center gap-2 text-white">
                <input type="radio" value="USER" {...register('assignType')} onChange={() => setValue('ownerId', '')} />
                Single Employee
              </label>
              <label className="flex items-center gap-2 text-white">
                <input type="radio" value="DEPARTMENT" {...register('assignType')} onChange={() => setValue('ownerId', '')} />
                Entire Department
              </label>
            </div>

            {assignType === 'USER' ? (
              <select 
                {...register('ownerId')}
                className={`w-full bg-gray-900 border rounded-lg p-3 text-white outline-none focus:ring-2 focus:ring-primary-500 ${errors.ownerId ? 'border-red-500' : 'border-gray-700'}`}
                disabled={loadingUsers}
              >
                <option value="">-- Select Employee --</option>
                {users?.map((u: any) => (
                  <option key={u.id} value={u.id}>{u.fullName} ({u.email})</option>
                ))}
              </select>
            ) : (
              <select 
                {...register('ownerId')}
                className={`w-full bg-gray-900 border rounded-lg p-3 text-white outline-none focus:ring-2 focus:ring-primary-500 ${errors.ownerId ? 'border-red-500' : 'border-gray-700'}`}
                disabled={loadingOrgUnits}
              >
                <option value="">-- Select Department --</option>
                {orgUnits?.map((ou: any) => (
                  <option key={ou.id} value={ou.id}>{ou.name} ({ou.code})</option>
                ))}
              </select>
            )}
            {errors.ownerId && <p className="text-red-400 text-xs mt-1">{errors.ownerId.message}</p>}
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
              <label className="block text-sm font-medium text-gray-300 mb-2">Frequency</label>
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
              disabled={createMutation.isPending}
              className="flex items-center gap-2 px-6 py-2 bg-primary-600 hover:bg-primary-500 text-white font-medium rounded-lg transition disabled:opacity-50"
            >
              {createMutation.isPending && <Loader2 size={18} className="animate-spin" />}
              Create KPI
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
