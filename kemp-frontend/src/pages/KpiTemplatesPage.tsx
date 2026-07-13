import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '../lib/apiClient';
import { PageHeader } from '../components/PageHeader';
import { DataTable } from '../components/DataTable';
import type { Column } from '../components/DataTable';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { Plus, Edit2, Trash2 } from 'lucide-react';
import toast from 'react-hot-toast';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';

interface KpiTemplate {
  id: string;
  name: string;
  description: string;
  category: string;
  defaultFrequency: string;
  defaultTarget: number;
  defaultFormula: string;
}

const templateSchema = z.object({
  name: z.string().min(1, 'Name is required'),
  description: z.string().optional(),
  category: z.string().min(1, 'Category is required'),
  defaultFrequency: z.string().min(1, 'Frequency is required'),
  defaultTarget: z.number().min(0, 'Target must be positive'),
  defaultFormula: z.string().optional()
});

type TemplateFormValues = z.infer<typeof templateSchema>;

export default function KpiTemplatesPage() {
  const queryClient = useQueryClient();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingTemplate, setEditingTemplate] = useState<KpiTemplate | null>(null);

  const { data: templates, isLoading } = useQuery<KpiTemplate[]>({
    queryKey: ['kpi-templates'],
    queryFn: async () => (await apiClient.get('/v1/kpi-templates')).data
  });

  const { register, handleSubmit, formState: { errors }, reset, setValue } = useForm<TemplateFormValues>({
    resolver: zodResolver(templateSchema),
    defaultValues: {
      name: '',
      description: '',
      category: 'Sales',
      defaultFrequency: 'MONTHLY',
      defaultTarget: 100,
      defaultFormula: ''
    }
  });

  const openCreateModal = () => {
    setEditingTemplate(null);
    reset({
      name: '',
      description: '',
      category: 'Sales',
      defaultFrequency: 'MONTHLY',
      defaultTarget: 100,
      defaultFormula: ''
    });
    setIsModalOpen(true);
  };

  const openEditModal = (tpl: KpiTemplate) => {
    setEditingTemplate(tpl);
    setValue('name', tpl.name);
    setValue('description', tpl.description || '');
    setValue('category', tpl.category);
    setValue('defaultFrequency', tpl.defaultFrequency);
    setValue('defaultTarget', tpl.defaultTarget);
    setValue('defaultFormula', tpl.defaultFormula || '');
    setIsModalOpen(true);
  };

  const saveMutation = useMutation({
    mutationFn: async (data: TemplateFormValues) => {
      if (editingTemplate) {
        return await apiClient.put(`/v1/kpi-templates/${editingTemplate.id}`, data);
      } else {
        return await apiClient.post('/v1/kpi-templates', data);
      }
    },
    onSuccess: () => {
      toast.success(editingTemplate ? 'Template updated successfully!' : 'Template created successfully!');
      queryClient.invalidateQueries({ queryKey: ['kpi-templates'] });
      setIsModalOpen(false);
    },
    onError: (err: any) => {
      toast.error(err.response?.data?.message || 'Failed to save template');
    }
  });

  const deleteMutation = useMutation({
    mutationFn: async (id: string) => {
      return await apiClient.delete(`/v1/kpi-templates/${id}`);
    },
    onSuccess: () => {
      toast.success('Template deleted successfully');
      queryClient.invalidateQueries({ queryKey: ['kpi-templates'] });
    },
    onError: (err: any) => {
      toast.error(err.response?.data?.message || 'Failed to delete template');
    }
  });

  const columns: Column<KpiTemplate>[] = [
    { header: 'Template Name', accessor: 'name' },
    { header: 'Category', accessor: 'category' },
    { header: 'Default Frequency', accessor: 'defaultFrequency' },
    { header: 'Default Target', accessor: 'defaultTarget' },
    {
      header: 'Actions',
      render: (tpl) => (
        <div className="flex gap-2">
          <button
            onClick={() => openEditModal(tpl)}
            className="p-1.5 hover:bg-gray-700 rounded text-blue-400 transition"
          >
            <Edit2 size={14} />
          </button>
          <button
            onClick={() => {
              if (confirm('Are you sure you want to delete this template?')) {
                deleteMutation.mutate(tpl.id);
              }
            }}
            className="p-1.5 hover:bg-gray-700 rounded text-red-400 transition"
          >
            <Trash2 size={14} />
          </button>
        </div>
      )
    }
  ];

  if (isLoading) return <LoadingSpinner />;

  return (
    <div className="space-y-8">
      <PageHeader 
        title="KPI Templates"
        breadcrumbs={[{ label: 'KPI Templates' }]}
        action={
          <button 
            onClick={openCreateModal}
            className="flex items-center gap-2 bg-blue-600 hover:bg-blue-500 text-white px-4 py-2 rounded-lg font-medium transition"
          >
            <Plus size={18} /> Add KPI Template
          </button>
        }
      />

      <div className="glass rounded-xl overflow-hidden">
        <DataTable data={templates || []} columns={columns} />
      </div>

      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm">
          <div className="w-full max-w-md bg-gray-800 border border-gray-700 rounded-xl shadow-2xl p-6 overflow-hidden">
            <h3 className="text-lg font-bold text-white mb-4">
              {editingTemplate ? 'Edit KPI Template' : 'Create KPI Template'}
            </h3>
            <form onSubmit={handleSubmit((d) => saveMutation.mutate(d))} className="space-y-4">
              <div>
                <label className="block text-sm text-gray-300 mb-1">Template Name</label>
                <input 
                  type="text" 
                  {...register('name')}
                  className={`w-full bg-gray-900 border rounded-lg p-2.5 text-white outline-none focus:ring-2 focus:ring-blue-500 ${errors.name ? 'border-red-500' : 'border-gray-700'}`}
                  placeholder="e.g. Sales Revenue"
                />
                {errors.name && <p className="text-red-400 text-xs mt-1">{errors.name.message}</p>}
              </div>

              <div>
                <label className="block text-sm text-gray-300 mb-1">Description</label>
                <textarea 
                  {...register('description')}
                  className="w-full bg-gray-900 border border-gray-700 rounded-lg p-2.5 text-white outline-none focus:ring-2 focus:ring-blue-500 h-20 resize-none"
                  placeholder="Template details..."
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm text-gray-300 mb-1">Category</label>
                  <input 
                    type="text" 
                    {...register('category')}
                    className="w-full bg-gray-900 border border-gray-700 rounded-lg p-2.5 text-white outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>

                <div>
                  <label className="block text-sm text-gray-300 mb-1">Frequency</label>
                  <select 
                    {...register('defaultFrequency')}
                    className="w-full bg-gray-900 border border-gray-700 rounded-lg p-2.5 text-white outline-none focus:ring-2 focus:ring-blue-500"
                  >
                    <option value="DAILY">Daily</option>
                    <option value="WEEKLY">Weekly</option>
                    <option value="MONTHLY">Monthly</option>
                    <option value="QUARTERLY">Quarterly</option>
                    <option value="YEARLY">Yearly</option>
                  </select>
                </div>
              </div>

              <div>
                <label className="block text-sm text-gray-300 mb-1">Default Target</label>
                <input 
                  type="number" 
                  {...register('defaultTarget', { valueAsNumber: true })}
                  className="w-full bg-gray-900 border border-gray-700 rounded-lg p-2.5 text-white outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>

              <div>
                <label className="block text-sm text-gray-300 mb-1">Calculation Formula (Optional)</label>
                <input 
                  type="text" 
                  {...register('defaultFormula')}
                  className="w-full bg-gray-900 border border-gray-700 rounded-lg p-2.5 text-white outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="e.g. A + B / C"
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
                  disabled={saveMutation.isPending}
                  className="px-4 py-2 bg-blue-600 hover:bg-blue-500 text-white rounded-lg text-sm font-medium transition disabled:opacity-50"
                >
                  {saveMutation.isPending ? 'Saving...' : 'Save'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
