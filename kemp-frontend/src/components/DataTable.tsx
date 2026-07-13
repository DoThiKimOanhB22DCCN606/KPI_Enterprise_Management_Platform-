import React from 'react';
import { LoadingSpinner } from './LoadingSpinner';

export interface Column<T> {
  header: string;
  accessor?: keyof T;
  render?: (item: T) => React.ReactNode;
}

interface DataTableProps<T> {
  columns: Column<T>[];
  data: T[];
  loading?: boolean;
  onRowClick?: (item: T) => void;
  emptyMessage?: string;
}

export function DataTable<T>({ columns, data, loading, onRowClick, emptyMessage = 'No records found' }: DataTableProps<T>) {
  if (loading) {
    return <div className="p-8 flex justify-center"><LoadingSpinner /></div>;
  }

  if (!data || data.length === 0) {
    return (
      <div className="bg-gray-800 rounded-lg border border-gray-700 p-8 text-center text-gray-400">
        {emptyMessage}
      </div>
    );
  }

  return (
    <div className="overflow-x-auto bg-gray-800 rounded-lg border border-gray-700 shadow">
      <table className="w-full text-left border-collapse">
        <thead>
          <tr className="border-b border-gray-700 bg-gray-800/50 text-gray-400 text-sm">
            {columns.map((col, idx) => (
              <th key={idx} className="py-3 px-4 font-medium whitespace-nowrap">{col.header}</th>
            ))}
          </tr>
        </thead>
        <tbody className="text-sm">
          {data.map((row, rowIdx) => (
            <tr 
              key={rowIdx} 
              onClick={() => onRowClick?.(row)}
              className={`border-b border-gray-700/50 hover:bg-gray-700/50 transition-colors ${onRowClick ? 'cursor-pointer' : ''}`}
            >
              {columns.map((col, colIdx) => (
                <td key={colIdx} className="py-3 px-4 text-gray-200">
                  {col.render ? col.render(row) : col.accessor ? String(row[col.accessor] ?? '') : ''}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
