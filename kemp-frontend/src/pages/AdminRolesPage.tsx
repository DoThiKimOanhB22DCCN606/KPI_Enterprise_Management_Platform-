import { useState } from 'react';
import { PageHeader } from '../components/PageHeader';
import { DataTable } from '../components/DataTable';
import type { Column } from '../components/DataTable';
import { Plus, Shield, Edit, Trash2 } from 'lucide-react';
import toast from 'react-hot-toast';

interface Role {
  id: string;
  code: string;
  name: string;
  description: string;
}

const mockRoles: Role[] = [
  { id: '1', code: 'TENANT_ADMIN', name: 'Tenant Administrator', description: 'Full access to all tenant resources' },
  { id: '2', code: 'HR_ADMIN', name: 'HR Administrator', description: 'Access to user and organization management' },
  { id: '3', code: 'TEAM_LEADER', name: 'Team Leader', description: 'Can manage team KPIs and approvals' },
  { id: '4', code: 'EMPLOYEE', name: 'Employee', description: 'Standard user access' },
];

export default function AdminRolesPage() {
  const [showAdd, setShowAdd] = useState(false);
  const [roles, setRoles] = useState<Role[]>(mockRoles);
  const [formData, setFormData] = useState({ code: '', name: '', description: '' });

  const handleSave = () => {
    if (!formData.code || !formData.name) {
      toast.error('Code and Name are required');
      return;
    }
    
    setRoles([...roles, { ...formData, id: Date.now().toString() }]);
    toast.success('Role saved successfully');
    setShowAdd(false);
    setFormData({ code: '', name: '', description: '' });
  };

  const columns: Column<Role>[] = [
    { header: 'Role Name', accessor: 'name', render: (r) => (
      <div className="flex items-center gap-2">
        <Shield size={16} className="text-gray-400" />
        <span className="font-medium text-white">{r.name}</span>
      </div>
    )},
    { header: 'Code', accessor: 'code', render: (r) => <span className="text-gray-400 font-mono text-xs">{r.code}</span> },
    { header: 'Description', accessor: 'description', render: (r) => <span className="text-gray-400">{r.description}</span> }
  ];

  return (
    <div className="p-8 max-w-7xl mx-auto">
      <PageHeader title="Role & Position Management" />

      <div className="bg-gray-800 border border-gray-700 rounded-xl overflow-hidden mt-6">
        <DataTable columns={columns} data={roles} loading={false} />
      </div>


    </div>
  );
}
