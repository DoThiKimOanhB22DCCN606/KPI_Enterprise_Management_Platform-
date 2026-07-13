import React from 'react';
import clsx from 'clsx';

interface KpiStatusBadgeProps {
  status: string;
}

export const KpiStatusBadge: React.FC<KpiStatusBadgeProps> = ({ status }) => {
  const getBadgeStyle = (status: string) => {
    switch (status?.toUpperCase()) {
      case 'DRAFT': return 'bg-gray-500/10 text-gray-400 border-gray-500/20';
      case 'PENDING':
      case 'PENDING_MANAGER':
      case 'PENDING_DIRECTOR': return 'bg-yellow-500/10 text-yellow-400 border-yellow-500/20';
      case 'APPROVED': return 'bg-blue-500/10 text-blue-400 border-blue-500/20';
      case 'ACTIVE': return 'bg-green-500/10 text-green-400 border-green-500/20';
      case 'COMPLETED': return 'bg-teal-500/10 text-teal-400 border-teal-500/20';
      case 'CLOSED': return 'bg-red-500/10 text-red-400 border-red-500/20';
      case 'ARCHIVED': return 'bg-slate-700/50 text-slate-400 border-slate-600/50';
      default: return 'bg-gray-500/10 text-gray-400 border-gray-500/20';
    }
  };

  return (
    <span className={clsx("text-xs px-2.5 py-1 rounded-full border font-medium", getBadgeStyle(status))}>
      {status || 'UNKNOWN'}
    </span>
  );
};
