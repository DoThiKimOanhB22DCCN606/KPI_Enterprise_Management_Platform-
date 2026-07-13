import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { apiClient } from '../lib/apiClient';
import { PageHeader } from '../components/PageHeader';
import type { Column } from '../components/DataTable';
import { ChevronDown, ChevronUp } from 'lucide-react';

interface AuditLog {
  id: string;
  timestamp: string;
  userId: string;
  action: string;
  resourceType: string;
  resourceId: string;
  ipAddress: string;
  oldData?: any;
  newData?: any;
}

const ExpandedRow: React.FC<{ log: AuditLog }> = ({ log }) => {
  return (
    <div className="p-4 bg-gray-900 border-b border-gray-700 grid grid-cols-2 gap-4 text-xs font-mono">
      <div className="bg-gray-800 p-3 rounded overflow-auto max-h-40">
        <div className="text-gray-400 mb-1 font-bold">Old Data:</div>
        <pre className="text-red-400">{JSON.stringify(log.oldData, null, 2)}</pre>
      </div>
      <div className="bg-gray-800 p-3 rounded overflow-auto max-h-40">
        <div className="text-gray-400 mb-1 font-bold">New Data:</div>
        <pre className="text-green-400">{JSON.stringify(log.newData, null, 2)}</pre>
      </div>
    </div>
  );
};

export default function AuditLogPage() {
  const [expandedId, setExpandedId] = useState<string | null>(null);

  const { data: logs, isLoading } = useQuery({
    queryKey: ['auditLogs'],
    queryFn: async () => (await apiClient.get('/v1/audit-logs')).data.content as AuditLog[]
  });

  const columns: Column<AuditLog>[] = [
    { header: '', render: (l) => (
        <button onClick={(e) => { e.stopPropagation(); setExpandedId(expandedId === l.id ? null : l.id); }} className="text-gray-400">
          {expandedId === l.id ? <ChevronUp size={16} /> : <ChevronDown size={16} />}
        </button>
      )
    },
    { header: 'Timestamp', render: (l) => <span className="text-gray-400 text-xs">{new Date(l.timestamp).toLocaleString()}</span> },
    { header: 'Action', render: (l) => <span className="font-bold text-gray-200">{l.action}</span> },
    { header: 'Resource Type', accessor: 'resourceType' },
    { header: 'Resource ID', render: (l) => <span className="font-mono text-xs text-gray-400">{l.resourceId.substring(0,8)}...</span> },
    { header: 'User', render: (l) => <span className="font-mono text-xs text-primary-400">{l.userId.substring(0,8)}...</span> },
    { header: 'IP Address', accessor: 'ipAddress' }
  ];

  return (
    <div className="p-8 max-w-7xl mx-auto">
      <PageHeader title="Audit Logs" />

      <div className="mb-6 flex gap-4">
        <input type="text" placeholder="Filter by User ID" className="bg-gray-800 border border-gray-700 text-white rounded-lg px-4 py-2 text-sm w-48" />
        <input type="date" className="bg-gray-800 border border-gray-700 text-white rounded-lg px-4 py-2 text-sm" />
      </div>

      <div className="bg-gray-800 rounded-lg border border-gray-700 overflow-hidden">
        <table className="w-full text-left">
          <thead className="bg-gray-800/50 border-b border-gray-700 text-gray-400 text-sm">
            <tr>
              {columns.map((c, i) => <th key={i} className="p-3 font-medium">{c.header}</th>)}
            </tr>
          </thead>
          <tbody className="text-sm">
            {logs?.map((log) => (
              <React.Fragment key={log.id}>
                <tr className="border-b border-gray-700/50 hover:bg-gray-700/50 transition">
                  {columns.map((c, i) => <td key={i} className="p-3 text-gray-200">{c.render ? c.render(log) : c.accessor ? String(log[c.accessor]) : ''}</td>)}
                </tr>
                {expandedId === log.id && (
                  <tr>
                    <td colSpan={columns.length} className="p-0"><ExpandedRow log={log} /></td>
                  </tr>
                )}
              </React.Fragment>
            ))}
          </tbody>
        </table>
        {(!logs || logs.length === 0) && !isLoading && (
          <div className="p-8 text-center text-gray-500">No audit logs found.</div>
        )}
      </div>
    </div>
  );
}
