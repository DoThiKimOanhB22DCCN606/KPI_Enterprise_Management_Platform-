import React, { useState, useRef } from 'react';
import { UploadCloud, File as FileIcon } from 'lucide-react';
import { apiClient } from '../lib/apiClient';
import toast from 'react-hot-toast';

interface FileDropzoneProps {
  kpiId: string;
  valueId: string;
}

export function FileDropzone({ kpiId, valueId }: FileDropzoneProps) {
  const [isDragging, setIsDragging] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [attachments, setAttachments] = useState<any[]>([]);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(true);
  };

  const handleDragLeave = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(false);
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(false);
    
    if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
      handleFiles(Array.from(e.dataTransfer.files));
    }
  };

  const handleFileInput = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files.length > 0) {
      handleFiles(Array.from(e.target.files));
    }
  };

  const handleFiles = async (files: File[]) => {
    setUploading(true);
    for (const file of files) {
      try {
        const formData = new FormData();
        formData.append('file', file);
        const res = await apiClient.post(`/v1/kpis/${kpiId}/values/${valueId}/attachments`, formData);
        setAttachments(prev => [...prev, res.data]);
        toast.success(`Uploaded ${file.name}`);
      } catch (err: any) {
        toast.error(`Failed to upload ${file.name}: ${err.response?.data?.message || err.message}`);
      }
    }
    setUploading(false);
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  return (
    <div className="space-y-4 mt-4">
      <div 
        className={`border-2 border-dashed rounded-xl p-6 text-center transition-colors cursor-pointer
          ${isDragging ? 'border-blue-500 bg-blue-500/10' : 'border-gray-600 hover:border-gray-500 bg-gray-800/30'}`}
        onDragOver={handleDragOver}
        onDragLeave={handleDragLeave}
        onDrop={handleDrop}
        onClick={() => fileInputRef.current?.click()}
      >
        <UploadCloud className={`w-10 h-10 mx-auto mb-2 ${isDragging ? 'text-blue-400' : 'text-gray-400'}`} />
        <p className="text-gray-300 font-medium">Click or drag files to upload evidence</p>
        <p className="text-sm text-gray-500 mt-1">Images, PDFs, or documents up to 10MB</p>
        <input 
          type="file" 
          ref={fileInputRef} 
          onChange={handleFileInput} 
          className="hidden" 
          multiple
        />
      </div>

      {uploading && (
        <div className="text-sm text-blue-400 animate-pulse">Uploading files...</div>
      )}

      {attachments.length > 0 && (
        <div className="space-y-2">
          <h4 className="text-sm font-medium text-gray-400">Attached Evidence</h4>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
            {attachments.map((att, idx) => (
              <div key={idx} className="flex items-center gap-3 p-3 bg-gray-800 rounded-lg border border-gray-700">
                <FileIcon className="w-5 h-5 text-blue-400" />
                <a href={att.url} target="_blank" rel="noopener noreferrer" className="text-sm text-gray-200 hover:text-white truncate flex-1">
                  {att.fileName}
                </a>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
