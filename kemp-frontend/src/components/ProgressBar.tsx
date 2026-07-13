import React from 'react';
import clsx from 'clsx';

interface ProgressBarProps {
  progress: number;
  showText?: boolean;
  colorClass?: string;
  className?: string;
}

export const ProgressBar: React.FC<ProgressBarProps> = ({ 
  progress, 
  showText = true, 
  colorClass,
  className 
}) => {
  const clampedProgress = Math.min(100, Math.max(0, progress));
  
  const defaultColor = clampedProgress < 50 ? 'bg-red-500' : 
                       clampedProgress < 80 ? 'bg-yellow-500' : 'bg-green-500';
                       
  const barColor = colorClass || defaultColor;

  return (
    <div className={clsx("w-full", className)}>
      {showText && (
        <div className="flex justify-between items-center mb-1 text-xs text-gray-400">
          <span>Progress</span>
          <span>{clampedProgress.toFixed(1)}%</span>
        </div>
      )}
      <div className="w-full bg-gray-800 rounded-full h-2 overflow-hidden border border-gray-700">
        <div 
          className={clsx("h-full transition-all duration-1000 ease-out", barColor)}
          style={{ width: `${clampedProgress}%` }}
        />
      </div>
    </div>
  );
};
