import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '../lib/apiClient';
import { PageHeader } from '../components/PageHeader';
import { DataTable } from '../components/DataTable';
import type { Column } from '../components/DataTable';
import { Plus, Building2, Loader2, Edit, Trash2 } from 'lucide-react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import toast from 'react-hot-toast';

const orgSchema = z.object({
  name: z.string().min(2, 'Name must be at least 2 characters'),
  description: z.string().optional(),
  parentId: z.string().optional()
});

type OrgFormValues = z.infer<typeof orgSchema>;

interface Organization {
  id: string;
  name: string;
  description?: string;
  parentId?: string;
  createdAt: string;
}

export default function OrganizationPage() {
  const [showModal, setShowModal] = useState(false);
  const [editingOrg, setEditingOrg] = useState<Organization | null>(null);
  const queryClient = useQueryClient();

  const { data: orgs, isLoading } = useQuery({
    queryKey: ['organizations'],
    queryFn: async () => {
      try {
        const response = await apiClient.get('/v1/organizations');
        // Handle both paginated response and list response
        return (response.data.content || response.data) as Organization[];
      } catch (error) {
        toast.error('Failed to load organizations');
        return [];
      }
    }
  });

  const { register, handleSubmit, formState: { errors }, reset } = useForm<OrgFormValues>({
    resolver: zodResolver(orgSchema),
    defaultValues: { name: '', description: '', parentId: '' }
  });

  const closeModal = () => {
    setShowModal(false);
    setEditingOrg(null);
    reset({ name: '', description: '', parentId: '' });
  };

  const openAddModal = () => {
    setEditingOrg(null);
    reset({ name: '', description: '', parentId: '' });
    setShowModal(true);
  };

  const openEditModal = (org: Organization) => {
    setEditingOrg(org);
    reset({
      name: org.name,
      description: org.description || '',
      parentId: org.parentId || ''
    });
    setShowModal(true);
  };

  const addMutation = useMutation({
    mutationFn: async (payload: any) => {
      return await apiClient.post('/v1/organizations', payload);
    },
    onSuccess: () => {
      toast.success('Department created successfully');
      queryClient.invalidateQueries({ queryKey: ['organizations'] });
      closeModal();
    },
    onError: (err: any) => {
      toast.error(err.response?.data?.message || 'Failed to create department');
    }
  });

  const editMutation = useMutation({
    mutationFn: async ({ id, payload }: { id: string, payload: any }) => {
      return await apiClient.put(`/v1/organizations/${id}`, payload);
    },
    onSuccess: () => {
      toast.success('Department updated successfully');
      queryClient.invalidateQueries({ queryKey: ['organizations'] });
      closeModal();
    },
    onError: (err: any) => {
      toast.error(err.response?.data?.message || 'Failed to update department');
    }
  });

  const deleteMutation = useMutation({
    mutationFn: async (id: string) => {
      return await apiClient.delete(`/v1/organizations/${id}`);
    },
    onSuccess: () => {
      toast.success('Department deleted successfully');
      queryClient.invalidateQueries({ queryKey: ['organizations'] });
    },
    onError: (err: any) => {
      toast.error(err.response?.data?.message || 'Failed to delete department');
    }
  });

  const onSubmit = (data: OrgFormValues) => {
    const payload = {
      name: data.name,
      description: data.description,
      parentId: data.parentId || null,
      type: 'DEPARTMENT',
      code: data.name.toUpperCase().replace(/\s+/g, '_')
    };

    if (editingOrg) {
      editMutation.mutate({ id: editingOrg.id, payload });
    } else {
      addMutation.mutate(payload);
    }
  };

  const handleDelete = (id: string) => {
    if (window.confirm('Are you sure you want to delete this department?')) {
      deleteMutation.mutate(id);
    }
  };

  const columns: Column<Organization>[] = [
    { header: 'Department Name', accessor: 'name', render: (o) => (
      <div className="flex items-center gap-2">
        <Building2 size={16} className="text-gray-400" />
        <span className="font-medium text-white">{o.name}</span>
      </div>
    )},
    { header: 'Description', accessor: 'description', render: (o) => <span className="text-gray-400">{o.description || '-'}</span> },
    { header: 'Parent', render: (o) => {
      if (!o.parentId) return <span className="text-gray-500 italic">None</span>;
      const parent = orgs?.find(p => p.id === o.parentId);
      return <span className="text-gray-300">{parent?.name || o.parentId}</span>;
    }},
    { header: 'Created', render: (o) => new Date(o.createdAt).toLocaleDateString() },
    { header: 'Actions', render: (o) => (
      <div className="flex gap-3 text-gray-400">
        <button 
          title="Edit" 
          onClick={() => openEditModal(o)}
          className="hover:text-white"
        >
          <Edit size={16} />
        </button>
        <button 
          title="Delete" 
          onClick={() => handleDelete(o.id)}
          className="hover:text-red-400"
        >
          <Trash2 size={16} />
        </button>
      </div>
    )}
  ];

  const isPending = addMutation.isPending || editMutation.isPending;

  return (
    <div className="p-8 max-w-7xl mx-auto">
      <PageHeader 
        title="Organization Structure" 
        action={
          <button 
            onClick={openAddModal}
            className="flex items-center gap-2 bg-primary-600 hover:bg-primary-500 text-white px-4 py-2 rounded-lg transition"
          >
            <Plus size={18} /> Add Department
          </button>
        }
      />

      <div className="bg-gray-800 border border-gray-700 rounded-xl overflow-hidden mt-6">
        <DataTable columns={columns} data={orgs || []} loading={isLoading} />
      </div>

      {showModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm">
          <div className="bg-gray-800 rounded-xl max-w-md w-full border border-gray-700 p-6">
            <h3 className="text-xl font-bold text-white mb-4">
              {editingOrg ? 'Edit Department' : 'Add New Department'}
            </h3>
            <form onSubmit={handleSubmit(onSubmit)}>
              <div className="space-y-4 mb-6">
                <div>
                  <label className="block text-sm text-gray-400 mb-1">Department Name *</label>
                  <input 
                    type="text" 
                    {...register('name')}
                    className={`w-full bg-gray-900 border rounded-lg p-2 text-white outline-none focus:ring-2 focus:ring-primary-500 ${errors.name ? 'border-red-500' : 'border-gray-700'}`} 
                    placeholder="e.g. Engineering"
                  />
                  {errors.name && <p className="text-red-400 text-xs mt-1">{errors.name.message}</p>}
                </div>
                
                <div>
                  <label className="block text-sm text-gray-400 mb-1">Description</label>
                  <textarea 
                    {...register('description')}
                    className="w-full bg-gray-900 border border-gray-700 rounded-lg p-2 text-white outline-none focus:ring-2 focus:ring-primary-500" 
                    placeholder="Optional description"
                    rows={3}
                  />
                </div>

                <div>
                  <label className="block text-sm text-gray-400 mb-1">Parent Department</label>
                  <select 
                    {...register('parentId')}
                    className="w-full bg-gray-900 border border-gray-700 rounded-lg p-2 text-white outline-none focus:ring-2 focus:ring-primary-500"
                  >
                    <option value="">-- None (Top Level) --</option>
                    {orgs?.filter(o => o.id !== editingOrg?.id).map(o => (
                      <option key={o.id} value={o.id}>{o.name}</option>
                    ))}
                  </select>
                </div>
              </div>
              <div className="flex justify-end gap-3">
                <button 
                  type="button"
                  onClick={closeModal} 
                  className="px-4 py-2 text-gray-300 hover:bg-gray-700 rounded-lg"
                >
                  Cancel
                </button>
                <button 
                  type="submit" 
                  disabled={isPending}
                  className="flex items-center gap-2 px-4 py-2 bg-primary-600 hover:bg-primary-500 text-white rounded-lg disabled:opacity-50"
                >
                  {isPending && <Loader2 size={16} className="animate-spin" />}
                  {editingOrg ? 'Save Changes' : 'Create'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
