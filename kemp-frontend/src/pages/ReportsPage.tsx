import { useState, useEffect } from 'react';
import { apiClient } from '../lib/apiClient';
import { PageHeader } from '../components/PageHeader';
import { DataTable } from '../components/DataTable';
import type { Column } from '../components/DataTable';
import { Download, Plus } from 'lucide-react';
import toast from 'react-hot-toast';

interface Report {
  id: string;
  name: string;
  format: string;
  status: string;
  createdAt: string;
  downloadUrl?: string;
}

export default function ReportsPage() {
  const [showModal, setShowModal] = useState(false);
  const [format, setFormat] = useState('PDF');
  const [period, setPeriod] = useState('CURRENT_MONTH');
  const [reports, setReports] = useState<Report[]>(() => {
    const saved = localStorage.getItem('kemp_reports');
    return saved ? JSON.parse(saved) : [];
  });

  useEffect(() => {
    localStorage.setItem('kemp_reports', JSON.stringify(reports));
  }, [reports]);

  // Poll for status of pending reports
  useEffect(() => {
    const pendingReports = reports.filter(r => r.status === 'PENDING' || r.status === 'PROCESSING');
    if (pendingReports.length === 0) return;

    const intervalId = setInterval(() => {
      pendingReports.forEach(async (report) => {
        try {
          const res = await apiClient.get(`/v1/reports/${report.id}/status`);
          const statusData = res.data;
          
          if (statusData.status === 'COMPLETED' || statusData.status === 'FAILED') {
            setReports(prev => prev.map(r => 
              r.id === report.id 
                ? { ...r, status: statusData.status, downloadUrl: statusData.downloadUrl } 
                : r
            ));
            if (statusData.status === 'COMPLETED') {
              toast.success(`Report ${report.format} generated successfully`);
            } else {
              toast.error(`Report generation failed`);
            }
          }
        } catch (err) {
          console.error('Failed to poll report status', err);
        }
      });
    }, 3000);

    return () => clearInterval(intervalId);
  }, [reports]);

  const handleDownload = (report: Report) => {
    if (report.downloadUrl) {
      // In a real system, downloadUrl might be a full URL like S3 link.
      // If it's just a file path from LocalFileSystemStorageAdapter, we might need a dedicated download endpoint.
      // For now, we will fallback to the synchronous export if downloadUrl isn't directly reachable.
      window.open(`http://localhost:8080/v1/reports/export?format=${report.format}&title=KEMP%20Report`, '_blank');
    }
  };

  const handleGenerate = async () => {
    try {
      const res = await apiClient.post('/v1/reports/generate', { 
        format, 
        period,
        tenantId: '00000000-0000-0000-0000-000000000001', // Mock tenant
        requestingUserId: '00000000-0000-0000-0000-000000000002', // Mock user
        title: `Report ${format}`
      });
      
      const newReport: Report = {
        id: res.data.requestId,
        name: `Performance Report - ${period}`,
        format,
        status: 'PENDING',
        createdAt: new Date().toISOString()
      };
      
      setReports(prev => [newReport, ...prev]);
      setShowModal(false);
      toast.success('Report generation started');
    } catch (e: any) {
      toast.error(e.response?.data?.message || 'Failed to generate report');
    }
  };

  const columns: Column<Report>[] = [
    { header: 'Report Name', accessor: 'name' },
    { header: 'Format', accessor: 'format' },
    { header: 'Date', render: (r) => new Date(r.createdAt).toLocaleString() },
    { header: 'Status', render: (r) => (
      <span className={`px-2 py-1 rounded text-xs ${r.status === 'COMPLETED' ? 'bg-green-500/10 text-green-400' : r.status === 'FAILED' ? 'bg-red-500/10 text-red-400' : 'bg-yellow-500/10 text-yellow-400'}`}>
        {r.status}
      </span>
    )},
    { header: 'Action', render: (r) => (
      r.status === 'COMPLETED' ? (
        <button onClick={() => handleDownload(r)} className="text-primary-400 hover:text-primary-300 transition-colors">
          <Download size={18} />
        </button>
      ) : r.status === 'FAILED' ? (
        <span className="text-red-500 text-sm">Failed</span>
      ) : (
        <span className="text-gray-500 text-sm animate-pulse">Processing...</span>
      )
    )}
  ];

  return (
    <div className="p-8 max-w-6xl mx-auto space-y-6">
      <PageHeader 
        title="Reports" 
        action={
          <button 
            onClick={() => setShowModal(true)}
            className="flex items-center gap-2 bg-primary-600 hover:bg-primary-500 text-white px-4 py-2 rounded-lg transition"
          >
            <Plus size={18} /> Generate Report
          </button>
        }
      />

      <div className="glass rounded-xl overflow-hidden p-6 border border-gray-800">
        <DataTable columns={columns} data={reports} loading={false} />
        {reports.length === 0 && (
          <div className="text-center py-10 text-gray-400">
            No reports generated yet. Click "Generate Report" to create one.
          </div>
        )}
      </div>

      {/* Generate Modal */}
      {showModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm">
          <div className="bg-gray-800 rounded-xl max-w-md w-full border border-gray-700 p-6 shadow-2xl">
            <h3 className="text-xl font-bold text-white mb-4">Generate Report</h3>
            <div className="space-y-4 mb-6">
              <div>
                <label className="block text-sm text-gray-400 mb-1">Format</label>
                <select 
                  value={format}
                  onChange={(e) => setFormat(e.target.value)}
                  className="w-full bg-gray-900 border border-gray-700 rounded-lg p-2 text-white focus:ring-2 focus:ring-primary-500 outline-none"
                >
                  <option value="PDF">PDF</option>
                  <option value="EXCEL">EXCEL</option>
                </select>
              </div>
              <div>
                <label className="block text-sm text-gray-400 mb-1">Period</label>
                <select 
                  value={period}
                  onChange={(e) => setPeriod(e.target.value)}
                  className="w-full bg-gray-900 border border-gray-700 rounded-lg p-2 text-white focus:ring-2 focus:ring-primary-500 outline-none"
                >
                  <option value="CURRENT_MONTH">This Month</option>
                  <option value="LAST_QUARTER">Last Quarter</option>
                  <option value="YTD">Year to Date</option>
                </select>
              </div>
            </div>
            <div className="flex justify-end gap-3">
              <button 
                onClick={() => setShowModal(false)} 
                className="px-4 py-2 text-gray-300 hover:bg-gray-700 rounded-lg transition"
              >
                Cancel
              </button>
              <button 
                onClick={handleGenerate} 
                className="px-4 py-2 bg-primary-600 hover:bg-primary-500 text-white rounded-lg transition shadow-lg shadow-primary-500/20"
              >
                Generate
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
