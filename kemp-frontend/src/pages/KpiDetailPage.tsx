import { useState } from 'react';
import { useParams } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '../lib/apiClient';
import { auth } from '../lib/auth';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import toast from 'react-hot-toast';
import { PageHeader } from '../components/PageHeader';
import { KpiStatusBadge } from '../components/KpiStatusBadge';
import { ProgressBar } from '../components/ProgressBar';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { FileDropzone } from '../components/FileDropzone';

const progressSchema = z.object({
  value: z.number().min(0, 'Value must be positive'),
  periodStart: z.string().min(1, 'Start date is required'),
  periodEnd: z.string().min(1, 'End date is required'),
  notes: z.string().optional()
});

type ProgressFormValues = z.infer<typeof progressSchema>;

export default function KpiDetailPage() {
  const { id } = useParams<{ id: string }>();
  const user = auth.getCurrentUser();
  const queryClient = useQueryClient();
  const [formValueId, setFormValueId] = useState(() => crypto.randomUUID());
  const [showEvalModal, setShowEvalModal] = useState(false);
  const [evalNotes, setEvalNotes] = useState('');
  const [evalScore, setEvalScore] = useState<number | ''>('');

  const { data: kpi, isLoading: kpiLoading } = useQuery({
    queryKey: ['kpi', id],
    queryFn: async () => (await apiClient.get(`/v1/kpis/${id}`)).data
  });

  const { data: trend } = useQuery({
    queryKey: ['kpi-trend', id],
    queryFn: async () => (await apiClient.get(`/v1/analytics/kpis/${id}/trend`)).data
  });

  const { data: cycle } = useQuery({
    queryKey: ['cycle', kpi?.cycleId],
    queryFn: async () => (await apiClient.get(`/v1/cycles/${kpi.cycleId}`)).data,
    enabled: !!kpi?.cycleId
  });

  const { register, handleSubmit, formState: { errors }, reset } = useForm<ProgressFormValues>({
    resolver: zodResolver(progressSchema),
    defaultValues: {
      value: 0,
      periodStart: new Date(new Date().getFullYear(), new Date().getMonth(), 1).toISOString().split('T')[0],
      periodEnd: new Date(new Date().getFullYear(), new Date().getMonth() + 1, 0).toISOString().split('T')[0],
      notes: ''
    }
  });

  const progressMutation = useMutation({
    mutationFn: async (data: ProgressFormValues) => {
      return await apiClient.put(`/v1/kpis/${id}/progress`, {
        value: data.value,
        periodStart: new Date(data.periodStart).toISOString(),
        periodEnd: new Date(data.periodEnd).toISOString(),
        notes: data.notes,
        valueId: formValueId
      });
    },
    onSuccess: () => {
      toast.success('Progress updated successfully!');
      queryClient.invalidateQueries({ queryKey: ['kpi', id] });
      queryClient.invalidateQueries({ queryKey: ['kpi-trend', id] });
      reset();
      setFormValueId(crypto.randomUUID());
    },
    onError: (err: any) => {
      toast.error(err.response?.data?.message || 'Failed to record progress');
    }
  });

  const onProgressSubmit = (data: ProgressFormValues) => {
    progressMutation.mutate(data);
  };



  const handleApprove = async () => {
    try {
      await apiClient.post(`/v1/kpis/${id}/approvals`, { action: 'APPROVE' });
      toast.success('KPI approved successfully!');
      queryClient.invalidateQueries({ queryKey: ['kpi', id] });
    } catch (e: any) {
      toast.error(e.response?.data?.message || 'Failed to approve KPI');
    }
  };

  const handleReject = async () => {
    try {
      await apiClient.post(`/v1/kpis/${id}/approvals`, { action: 'REJECT', comment: 'Rejected by reviewer' });
      toast.success('KPI rejected successfully!');
      queryClient.invalidateQueries({ queryKey: ['kpi', id] });
    } catch (e: any) {
      toast.error(e.response?.data?.message || 'Failed to reject KPI');
    }
  };

  const handleSubmitForApproval = async () => {
    try {
      await apiClient.post(`/v1/kpis/${id}/approvals`, { action: 'SUBMIT' });
      toast.success('KPI submitted for approval successfully!');
      queryClient.invalidateQueries({ queryKey: ['kpi', id] });
    } catch (e: any) {
      toast.error(e.response?.data?.message || 'Failed to submit KPI');
    }
  };

  const handleEvaluate = async () => {
    try {
      // Simulate evaluation API call
      await new Promise(resolve => setTimeout(resolve, 500));
      toast.success('KPI Evaluated Successfully');
      setShowEvalModal(false);
      // We could also record the final progress here if needed
      if (evalScore !== '') {
        await apiClient.post(`/v1/kpis/${id}/complete`, {
          finalScore: Number(evalScore),
          managerComments: evalNotes
        });
        queryClient.invalidateQueries({ queryKey: ['kpi', id] });
      }
    } catch (e: any) {
      toast.error(e.response?.data?.message || 'Failed to evaluate KPI');
    }
  };

  if (kpiLoading) return <LoadingSpinner />;
  if (!kpi) return <div className="p-8 text-center text-gray-400">KPI Not Found</div>;

  const pct = kpi.target > 0 ? (kpi.currentProgress / kpi.target) * 100 : 0;
  const isPending = kpi.status === 'PENDING' || kpi.status === 'PENDING_MANAGER' || kpi.status === 'PENDING_DIRECTOR';
  const isNotOwner = user?.id !== kpi.ownerId;

  return (
    <div className="p-8 max-w-7xl mx-auto space-y-8">
      <PageHeader 
        title={kpi.name}
        breadcrumbs={[{ label: 'KPIs', href: '/kpis' }, { label: kpi.name }]}
        action={
          <div className="flex flex-col items-end gap-2">
            <div className="flex items-center gap-4">
              {kpi.status === 'ACTIVE' && (cycle?.status !== 'CLOSED') && (
                <button 
                  onClick={() => setShowEvalModal(true)}
                  className="px-4 py-2 bg-purple-600 hover:bg-purple-500 text-white rounded-lg transition text-sm font-medium"
                >
                  End-Period Evaluation
                </button>
              )}
              <KpiStatusBadge status={kpi.status} />
            </div>
            {cycle && (
              <div className="text-xs text-gray-400 text-right bg-gray-900/50 px-3 py-1.5 rounded-md border border-gray-800">
                <span className="font-medium text-gray-300">Cycle: {cycle.name}</span>
                <span className="mx-2">•</span>
                <span className={cycle.status === 'OPEN' ? 'text-emerald-400' : 'text-gray-500'}>{cycle.status}</span>
                <span className="mx-2">•</span>
                {new Date(cycle.periodStart).toLocaleDateString()} - {new Date(cycle.periodEnd).toLocaleDateString()}
              </div>
            )}
          </div>
        }
      />

      {isPending && isNotOwner && (
        <div className="bg-yellow-500/10 border border-yellow-500/20 rounded-xl p-4 flex justify-between items-center">
          <span className="text-yellow-400 font-medium">This KPI is awaiting your approval.</span>
          <div className="space-x-3">
            <button onClick={handleReject} className="px-4 py-2 bg-red-900/20 hover:bg-red-900/40 text-red-400 border border-red-500/20 rounded-lg transition">Reject</button>
            <button onClick={handleApprove} className="px-4 py-2 bg-blue-600 hover:bg-blue-500 text-white rounded-lg transition">Approve</button>
          </div>
        </div>
      )}

      {isPending && !isNotOwner && (
        <div className="bg-yellow-500/10 border border-yellow-500/20 rounded-xl p-4">
          <span className="text-yellow-400 font-medium">This KPI has been submitted and is awaiting approval from your manager.</span>
        </div>
      )}

      {kpi.status === 'DRAFT' && !isNotOwner && (
        <div className="bg-blue-500/10 border border-blue-500/20 rounded-xl p-4 flex justify-between items-center">
          <span className="text-blue-400 font-medium">This KPI is a draft. Submit it to start the approval workflow.</span>
          <button onClick={handleSubmitForApproval} className="px-4 py-2 bg-blue-600 hover:bg-blue-500 text-white rounded-lg transition">
            Submit for Approval
          </button>
        </div>
      )}

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="col-span-2 glass rounded-xl p-6">
          <h3 className="text-lg font-bold mb-4">Progress Trend</h3>
          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={trend || []}>
                <CartesianGrid strokeDasharray="3 3" stroke="#374151" />
                <XAxis dataKey="month" stroke="#9ca3af" />
                <YAxis stroke="#9ca3af" />
                <Tooltip contentStyle={{ backgroundColor: '#1f2937', borderColor: '#374151', color: '#f3f4f6' }} />
                <Line type="monotone" dataKey="avgValue" stroke="#3b82f6" strokeWidth={3} dot={{ r: 4 }} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>

        <div className="glass rounded-xl p-6 flex flex-col justify-center">
          <h3 className="text-lg font-bold mb-4 text-center">Current Progress</h3>
          <div className="text-center mb-6">
            <div className="text-4xl font-bold text-primary-400 mb-2">{kpi.currentProgress?.toLocaleString() ?? 0}</div>
            <div className="text-gray-400 text-sm">of {kpi.target?.toLocaleString() ?? 0}</div>
          </div>
          <ProgressBar progress={pct} />
        </div>
      </div>

      {kpi.status === 'ACTIVE' && cycle?.status !== 'CLOSED' && (
        <div className="glass rounded-xl p-6">
          <h3 className="text-lg font-bold mb-4">Record Progress</h3>
          <form onSubmit={handleSubmit(onProgressSubmit)} className="flex flex-col gap-4">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 items-start">
              <div>
                <label className="block text-sm text-gray-400 mb-1">Actual Value</label>
                <input 
                  type="number" 
                  step="0.01"
                  {...register('value', { valueAsNumber: true })}
                  className={`w-full bg-gray-900 border rounded-lg p-2 text-white outline-none focus:ring-2 focus:ring-primary-500 ${errors.value ? 'border-red-500' : 'border-gray-700'}`} 
                />
                {errors.value && <p className="text-red-400 text-xs mt-1">{errors.value.message}</p>}
              </div>

              <div>
                <label className="block text-sm text-gray-400 mb-1">Period Start</label>
                <input 
                  type="date" 
                  {...register('periodStart')}
                  className={`w-full bg-gray-900 border rounded-lg p-2 text-white outline-none focus:ring-2 focus:ring-primary-500 ${errors.periodStart ? 'border-red-500' : 'border-gray-700'}`} 
                />
                {errors.periodStart && <p className="text-red-400 text-xs mt-1">{errors.periodStart.message}</p>}
              </div>

              <div>
                <label className="block text-sm text-gray-400 mb-1">Period End</label>
                <input 
                  type="date" 
                  {...register('periodEnd')}
                  className={`w-full bg-gray-900 border rounded-lg p-2 text-white outline-none focus:ring-2 focus:ring-primary-500 ${errors.periodEnd ? 'border-red-500' : 'border-gray-700'}`} 
                />
                {errors.periodEnd && <p className="text-red-400 text-xs mt-1">{errors.periodEnd.message}</p>}
              </div>
            </div>
            
            <div className="mt-2">
              <label className="block text-sm text-gray-400 mb-1">Notes (Optional)</label>
              <textarea 
                {...register('notes')}
                rows={3}
                placeholder="Describe your progress, blockers, or any relevant details..."
                className="w-full bg-gray-900 border border-gray-700 rounded-lg p-3 text-white outline-none focus:ring-2 focus:ring-primary-500 resize-none"
              />
            </div>
            
            <div className="mt-2">
              <label className="block text-sm text-gray-400 mb-1">Evidence (Optional)</label>
              <FileDropzone kpiId={id as string} valueId={formValueId} />
            </div>
            
            <div className="flex justify-end mt-4">
              <button 
                type="submit" 
                disabled={progressMutation.isPending}
                className="px-6 py-2 bg-primary-600 hover:bg-primary-500 text-white rounded-lg transition disabled:opacity-50"
              >
                {progressMutation.isPending ? 'Submitting...' : 'Submit Progress'}
              </button>
            </div>
          </form>
        </div>
      )}

      {kpi.values && kpi.values.length > 0 && (
        <div className="glass rounded-xl p-6">
          <h3 className="text-lg font-bold mb-4">Historical Progress</h3>
          <div className="space-y-4">
            {kpi.values.map((val: any) => (
              <div key={val.id} className="bg-gray-900 border border-gray-700 rounded-lg p-4">
                <div className="flex justify-between items-start mb-2">
                  <div>
                    <span className="text-sm text-gray-400">Period: </span>
                    <span className="text-sm font-medium">{new Date(val.periodStart).toLocaleDateString()} - {new Date(val.periodEnd).toLocaleDateString()}</span>
                  </div>
                  <div>
                    <span className="text-sm text-gray-400">Value: </span>
                    <span className="text-lg font-bold text-blue-400">{val.actualValue}</span>
                  </div>
                </div>
                {val.comment && (
                  <p className="text-sm text-gray-300 mt-2">{val.comment}</p>
                )}
                {val.attachments && val.attachments.length > 0 && (
                  <div className="mt-3 space-y-2">
                    <h4 className="text-xs font-medium text-gray-400">Evidence</h4>
                    <div className="flex flex-wrap gap-2">
                      {val.attachments.map((att: any) => (
                        <a key={att.id} href={att.url} target="_blank" rel="noopener noreferrer" className="flex items-center gap-2 px-3 py-1.5 bg-gray-800 hover:bg-gray-700 border border-gray-600 rounded-lg transition text-xs">
                          <svg className="w-4 h-4 text-blue-400" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15.172 7l-6.586 6.586a2 2 0 102.828 2.828l6.414-6.586a4 4 0 00-5.656-5.656l-6.415 6.585a6 6 0 108.486 8.486L20.5 13" /></svg>
                          <span className="text-gray-200">{att.fileName}</span>
                        </a>
                      ))}
                    </div>
                  </div>
                )}
              </div>
            ))}
          </div>
        </div>
      )}

      {kpi.status === 'COMPLETED' && kpi.evaluationScore != null && (
        <div className="glass rounded-xl p-6 border border-purple-500/30 bg-purple-900/10">
          <h3 className="text-lg font-bold mb-4 text-purple-300">Manager Evaluation</h3>
          <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
            <div className="bg-gray-900/50 border border-gray-700 rounded-lg p-4 flex flex-col justify-center items-center">
              <span className="text-sm text-gray-400 mb-1">Final Score</span>
              <span className="text-4xl font-bold text-primary-400">{kpi.evaluationScore}</span>
            </div>
            <div className="md:col-span-3 bg-gray-900/50 border border-gray-700 rounded-lg p-4">
              <span className="block text-sm text-gray-400 mb-2">Manager Comments</span>
              <p className="text-gray-200 whitespace-pre-wrap">{kpi.managerComments || 'No additional comments provided.'}</p>
            </div>
          </div>
        </div>
      )}

      {showEvalModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm">
          <div className="bg-gray-800 rounded-xl max-w-lg w-full border border-gray-700 p-6">
            <h3 className="text-xl font-bold text-white mb-4">End-Period KPI Evaluation</h3>
            <div className="space-y-4 mb-6">
              <div>
                <label className="block text-sm text-gray-400 mb-1">Final Score / Value</label>
                <input 
                  type="number" 
                  value={evalScore}
                  onChange={(e) => setEvalScore(e.target.value === '' ? '' : Number(e.target.value))}
                  className="w-full bg-gray-900 border border-gray-700 rounded-lg p-2 text-white outline-none focus:ring-2 focus:ring-primary-500"
                />
              </div>
              <div>
                <label className="block text-sm text-gray-400 mb-1">Manager Comments & Feedback</label>
                <textarea 
                  rows={4}
                  value={evalNotes}
                  onChange={(e) => setEvalNotes(e.target.value)}
                  placeholder="Provide comprehensive feedback for this cycle..."
                  className="w-full bg-gray-900 border border-gray-700 rounded-lg p-3 text-white outline-none focus:ring-2 focus:ring-primary-500 resize-none"
                />
              </div>
            </div>
            <div className="flex justify-end gap-3">
              <button 
                onClick={() => setShowEvalModal(false)} 
                className="px-4 py-2 text-gray-300 hover:bg-gray-700 rounded-lg"
              >
                Cancel
              </button>
              <button 
                onClick={handleEvaluate} 
                className="px-6 py-2 bg-purple-600 hover:bg-purple-500 text-white rounded-lg transition font-medium"
              >
                Complete Evaluation
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
