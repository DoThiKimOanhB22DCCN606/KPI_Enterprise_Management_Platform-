import React from 'react';
import { AlertTriangle } from 'lucide-react';

interface ErrorAlertProps {
  message: string;
}

export const ErrorAlert: React.FC<ErrorAlertProps> = ({ message }) => {
  if (!message) return null;

  return (
    <div className="bg-red-500/10 border border-red-500/20 rounded-lg p-4 flex items-start gap-3 my-4">
      <AlertTriangle className="h-5 w-5 text-red-400 shrink-0 mt-0.5" />
      <p className="text-sm text-red-400">{message}</p>
    </div>
  );
};
