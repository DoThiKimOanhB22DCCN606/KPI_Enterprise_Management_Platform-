import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '../lib/apiClient';
import { PageHeader } from '../components/PageHeader';
import { DataTable } from '../components/DataTable';
import type { Column } from '../components/DataTable';
import { UserPlus, Shield, Lock, Unlock, Ban, Loader2 } from 'lucide-react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import toast from 'react-hot-toast';

const userSchema = z.object({
  email: z.string().email('Invalid email address'),
  initialRole: z.string().min(1, 'Role is required')
});

type UserFormValues = z.infer<typeof userSchema>;

interface User {
  id: string;
  fullName: string;
  email: string;
  roles: string[];
  status: string;
  lastLoginAt?: string;
}

export default function AdminUsersPage() {
  const [showInvite, setShowInvite] = useState(false);
  const [editingRoleUser, setEditingRoleUser] = useState<User | null>(null);
  const [selectedRole, setSelectedRole] = useState('EMPLOYEE');
  const queryClient = useQueryClient();

  const { data: users, isLoading } = useQuery({
    queryKey: ['users'],
    queryFn: async () => (await apiClient.get('/v1/users')).data.content as User[]
  });

  const { register, handleSubmit, formState: { errors }, reset } = useForm<UserFormValues>({
    resolver: zodResolver(userSchema),
    defaultValues: { email: '', initialRole: 'EMPLOYEE' }
  });

  const inviteMutation = useMutation({
    mutationFn: async (data: UserFormValues) => {
      // POST /v1/users/invite to create/invite user
      return await apiClient.post('/v1/users/invite', {
        email: data.email,
        roleIds: ['00000000-0000-0000-0000-000000000001'] // Mock UUID for demo
      });
    },
    onSuccess: () => {
      toast.success('User invited successfully');
      queryClient.invalidateQueries({ queryKey: ['users'] });
      setShowInvite(false);
      reset();
    },
    onError: (err: any) => {
      toast.error(err.response?.data?.message || 'Failed to invite user');
    }
  });

  const roleMutation = useMutation({
    mutationFn: async (userId: string) => {
      return await apiClient.put(`/v1/users/${userId}/roles`, {
        roleIds: ['00000000-0000-0000-0000-000000000001'] // Mock UUID for demo
      });
    },
    onSuccess: () => {
      toast.success('Roles updated successfully');
      queryClient.invalidateQueries({ queryKey: ['users'] });
      setEditingRoleUser(null);
    },
    onError: (err: any) => {
      toast.error(err.response?.data?.message || 'Failed to update roles');
    }
  });

  const statusMutation = useMutation({
    mutationFn: async ({ userId, action }: { userId: string; action: 'lock' | 'unlock' | 'deactivate' }) => {
      return await apiClient.post(`/v1/users/${userId}/${action}`);
    },
    onSuccess: (_, variables) => {
      toast.success(`User ${variables.action}ed successfully`);
      queryClient.invalidateQueries({ queryKey: ['users'] });
    },
    onError: (err: any) => {
      toast.error(err.response?.data?.message || 'Failed to update user status');
    }
  });

  const onSubmit = (data: UserFormValues) => {
    inviteMutation.mutate(data);
  };

  const columns: Column<User>[] = [
    { header: 'Name', accessor: 'fullName', render: (u) => <div className="font-medium text-white">{u.fullName}</div> },
    { header: 'Email', accessor: 'email' },
    { header: 'Roles', render: (u) => (
      <div className="flex gap-1 flex-wrap">
        {u.roles?.map(r => <span key={r} className="bg-primary-900/50 text-primary-300 text-xs px-2 py-0.5 rounded border border-primary-800">{r}</span>)}
      </div>
    )},
    { header: 'Status', render: (u) => (
      <span className={`text-xs px-2 py-1 rounded-full ${
        u.status === 'ACTIVE' ? 'bg-green-500/10 text-green-400' : 
        u.status === 'LOCKED' ? 'bg-red-500/10 text-red-400' : 'bg-gray-500/10 text-gray-400'
      }`}>
        {u.status}
      </span>
    )},
    { header: 'Last Login', render: (u) => u.lastLoginAt ? new Date(u.lastLoginAt).toLocaleString() : 'Never' },
    { header: 'Actions', render: (u) => (
      <div className="flex gap-3 text-gray-400">
        <button 
          title="Change Roles" 
          onClick={() => { setEditingRoleUser(u); setSelectedRole(u.roles?.[0] || 'EMPLOYEE'); }}
          className="hover:text-white"
        >
          <Shield size={16} />
        </button>
        {u.status === 'LOCKED' ? 
          <button title="Unlock" onClick={() => statusMutation.mutate({ userId: u.id, action: 'unlock' })} className="hover:text-green-400"><Unlock size={16} /></button> :
          <button title="Lock" onClick={() => statusMutation.mutate({ userId: u.id, action: 'lock' })} className="hover:text-yellow-400"><Lock size={16} /></button>
        }
        <button 
          title="Deactivate" 
          onClick={() => {
            if (window.confirm('Are you sure you want to deactivate this user?')) {
              statusMutation.mutate({ userId: u.id, action: 'deactivate' });
            }
          }}
          className="hover:text-red-400"
        >
          <Ban size={16} />
        </button>
      </div>
    )}
  ];

  return (
    <div className="p-8 max-w-7xl mx-auto">
      <PageHeader 
        title="User Management" 
        action={
          <button 
            onClick={() => setShowInvite(true)}
            className="flex items-center gap-2 bg-primary-600 hover:bg-primary-500 text-white px-4 py-2 rounded-lg transition"
          >
            <UserPlus size={18} /> Invite User
          </button>
        }
      />

      <DataTable columns={columns} data={users || []} loading={isLoading} />

      {showInvite && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm">
          <div className="bg-gray-800 rounded-xl max-w-md w-full border border-gray-700 p-6">
            <h3 className="text-xl font-bold text-white mb-4">Invite New User</h3>
            <form onSubmit={handleSubmit(onSubmit)}>
              <div className="space-y-4 mb-6">
                <div>
                  <label className="block text-sm text-gray-400 mb-1">Email Address</label>
                  <input 
                    type="email" 
                    {...register('email')}
                    className={`w-full bg-gray-900 border rounded-lg p-2 text-white outline-none focus:ring-2 focus:ring-primary-500 ${errors.email ? 'border-red-500' : 'border-gray-700'}`} 
                    placeholder="user@example.com"
                  />
                  {errors.email && <p className="text-red-400 text-xs mt-1">{errors.email.message}</p>}
                </div>
                <div>
                  <label className="block text-sm text-gray-400 mb-1">Initial Role</label>
                  <select 
                    {...register('initialRole')}
                    className="w-full bg-gray-900 border border-gray-700 rounded-lg p-2 text-white outline-none focus:ring-2 focus:ring-primary-500"
                  >
                    <option value="EMPLOYEE">EMPLOYEE</option>
                    <option value="TEAM_LEADER">TEAM_LEADER</option>
                    <option value="STORE_MANAGER">STORE_MANAGER</option>
                    <option value="HR_ADMIN">HR_ADMIN</option>
                  </select>
                  {errors.initialRole && <p className="text-red-400 text-xs mt-1">{errors.initialRole.message}</p>}
                </div>
              </div>
              <div className="flex justify-end gap-3">
                <button 
                  type="button"
                  onClick={() => { setShowInvite(false); reset(); }} 
                  className="px-4 py-2 text-gray-300 hover:bg-gray-700 rounded-lg"
                >
                  Cancel
                </button>
                <button 
                  type="submit" 
                  disabled={inviteMutation.isPending}
                  className="flex items-center gap-2 px-4 py-2 bg-primary-600 hover:bg-primary-500 text-white rounded-lg disabled:opacity-50"
                >
                  {inviteMutation.isPending && <Loader2 size={16} className="animate-spin" />}
                  Send Invite
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Edit Role Modal */}
      {editingRoleUser && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm">
          <div className="bg-gray-800 rounded-xl max-w-md w-full border border-gray-700 p-6">
            <h3 className="text-xl font-bold text-white mb-4">Edit Role for {editingRoleUser.fullName || editingRoleUser.email}</h3>
            <div className="space-y-4 mb-6">
              <div>
                <label className="block text-sm text-gray-400 mb-1">Role</label>
                <select 
                  value={selectedRole}
                  onChange={(e) => setSelectedRole(e.target.value)}
                  className="w-full bg-gray-900 border border-gray-700 rounded-lg p-2 text-white outline-none focus:ring-2 focus:ring-primary-500"
                >
                  <option value="EMPLOYEE">EMPLOYEE</option>
                  <option value="TEAM_LEADER">TEAM_LEADER</option>
                  <option value="STORE_MANAGER">STORE_MANAGER</option>
                  <option value="HR_ADMIN">HR_ADMIN</option>
                </select>
              </div>
            </div>
            <div className="flex justify-end gap-3">
              <button 
                onClick={() => setEditingRoleUser(null)} 
                className="px-4 py-2 text-gray-300 hover:bg-gray-700 rounded-lg"
              >
                Cancel
              </button>
              <button 
                onClick={() => roleMutation.mutate(editingRoleUser.id)} 
                disabled={roleMutation.isPending}
                className="flex items-center gap-2 px-4 py-2 bg-primary-600 hover:bg-primary-500 text-white rounded-lg disabled:opacity-50"
              >
                {roleMutation.isPending && <Loader2 size={16} className="animate-spin" />}
                Update Role
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
