import React from 'react';
import { ChevronRight } from 'lucide-react';
import { Link } from 'react-router-dom';

interface PageHeaderProps {
  title: string;
  breadcrumbs?: Array<{ label: string; href?: string }>;
  action?: React.ReactNode;
}

export const PageHeader: React.FC<PageHeaderProps> = ({ title, breadcrumbs, action }) => {
  return (
    <div className="mb-6 flex flex-col md:flex-row md:items-center md:justify-between gap-4">
      <div>
        {breadcrumbs && breadcrumbs.length > 0 && (
          <nav className="flex items-center text-sm text-gray-400 mb-2 space-x-1">
            {breadcrumbs.map((bc, idx) => (
              <React.Fragment key={idx}>
                {bc.href ? (
                  <Link to={bc.href} className="hover:text-primary-400 transition-colors">{bc.label}</Link>
                ) : (
                  <span className="text-gray-300">{bc.label}</span>
                )}
                {idx < breadcrumbs.length - 1 && <ChevronRight className="h-4 w-4 text-gray-600" />}
              </React.Fragment>
            ))}
          </nav>
        )}
        <h1 className="text-2xl font-bold text-white tracking-tight">{title}</h1>
      </div>
      {action && <div>{action}</div>}
    </div>
  );
};
