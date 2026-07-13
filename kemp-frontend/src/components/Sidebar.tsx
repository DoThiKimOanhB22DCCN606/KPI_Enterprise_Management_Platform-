import React from 'react';
import { NavLink } from 'react-router-dom';
import { 
  Target, 
  LayoutDashboard, 
  Users, 
  LogOut, 
  Bell, 
  FileText, 
  History, 
  Building2, 
  Shield,
  Activity, 
  Trophy,
  Plus,
  Calendar,
  Copy
} from 'lucide-react';
import { auth } from '../lib/auth';

export const Sidebar: React.FC = () => {
  const user = auth.getCurrentUser();

  const hasRole = (allowedRoles: string[]) => {
    return user?.roles?.some((role: string) => allowedRoles.includes(role)) || false;
  };

  const navItems = [
    {
      label: 'Dashboard',
      to: '/dashboard',
      icon: LayoutDashboard,
    },
    {
      label: 'KPI List',
      to: '/kpis',
      icon: Activity,
    },
    {
      label: 'New KPI',
      to: '/kpis/new',
      icon: Plus,
    },
    {
      label: 'Goal Tree (OKR)',
      to: '/goals',
      icon: Target,
    },
    {
      label: 'Leaderboard',
      to: '/leaderboard',
      icon: Trophy,
    },
    {
      label: 'Reports',
      to: '/reports',
      icon: FileText,
    },
    {
      label: 'Dashboards',
      to: '/dashboards',
      icon: LayoutDashboard,
    },
  ];

  const adminItems = [
    {
      label: 'Evaluation Cycles',
      to: '/admin/cycles',
      icon: Calendar,
      allowedRoles: ['TENANT_ADMIN', 'HR_ADMIN'],
    },
    {
      label: 'KPI Templates',
      to: '/admin/kpi-templates',
      icon: Copy,
      allowedRoles: ['TENANT_ADMIN', 'HR_ADMIN'],
    },
    {
      label: 'Roles & Positions',
      to: '/admin/roles',
      icon: Shield,
      allowedRoles: ['TENANT_ADMIN', 'HR_ADMIN'],
    },
    {
      label: 'User Management',
      to: '/admin/users',
      icon: Users,
      allowedRoles: ['TENANT_ADMIN', 'HR_ADMIN'],
    },
    {
      label: 'Organization Units',
      to: '/admin/organizations',
      icon: Building2,
      allowedRoles: ['TENANT_ADMIN', 'HR_ADMIN'],
    },
    {
      label: 'Audit Logs',
      to: '/admin/audit',
      icon: History,
      allowedRoles: ['TENANT_ADMIN'],
    },
  ];

  const handleLogout = async () => {
    await auth.logout();
  };

  return (
    <aside className="w-64 bg-gray-800 border-r border-gray-700 flex flex-col h-screen sticky top-0">
      {/* Brand Logo */}
      <div className="flex items-center gap-3 px-6 h-16 border-b border-gray-700">
        <div className="bg-blue-500 rounded p-1.5 flex items-center justify-center">
          <Target className="h-5 w-5 text-white" />
        </div>
        <span className="font-bold text-lg tracking-tight text-white">KEMP KPI</span>
      </div>

      {/* Navigation */}
      <nav className="flex-1 px-4 py-6 space-y-7 overflow-y-auto">
        <div className="space-y-1">
          <p className="px-3 text-xs font-semibold text-gray-400 uppercase tracking-wider mb-2">Main Menu</p>
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) => 
                `flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors ${
                  isActive 
                    ? 'bg-blue-500/10 text-blue-400 border border-blue-500/20' 
                    : 'text-gray-300 hover:bg-gray-700 hover:text-white border border-transparent'
                }`
              }
            >
              <item.icon className="h-4 w-4 shrink-0" />
              <span>{item.label}</span>
            </NavLink>
          ))}
        </div>

        {/* Administration Section */}
        {adminItems.some(item => hasRole(item.allowedRoles)) && (
          <div className="space-y-1">
            <p className="px-3 text-xs font-semibold text-gray-400 uppercase tracking-wider mb-2">Administration</p>
            {adminItems.map((item) => {
              if (!hasRole(item.allowedRoles)) return null;
              return (
                <NavLink
                  key={item.to}
                  to={item.to}
                  className={({ isActive }) => 
                    `flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors ${
                      isActive 
                        ? 'bg-blue-500/10 text-blue-400 border border-blue-500/20' 
                        : 'text-gray-300 hover:bg-gray-700 hover:text-white border border-transparent'
                    }`
                  }
                >
                  <item.icon className="h-4 w-4 shrink-0" />
                  <span>{item.label}</span>
                </NavLink>
              );
            })}
          </div>
        )}
      </nav>

      {/* User Section / Footer */}
      <div className="p-4 border-t border-gray-700 bg-gray-800/80">
        <div className="flex items-center justify-between gap-2 mb-3">
          <div className="flex flex-col min-w-0">
            <span className="text-sm font-medium text-white truncate">{user?.fullName || 'User'}</span>
            <span className="text-xs text-gray-400 truncate">{user?.roles?.[0] || 'Employee'}</span>
          </div>
          <NavLink to="/notifications" className="text-gray-400 hover:text-white relative p-1.5 hover:bg-gray-700 rounded-lg transition-colors">
            <Bell className="h-4 w-4" />
          </NavLink>
        </div>
        <button
          onClick={handleLogout}
          className="w-full flex items-center justify-center gap-2 px-3 py-2 text-sm font-medium text-red-400 hover:text-red-300 hover:bg-red-500/10 border border-transparent hover:border-red-500/20 rounded-lg transition-colors"
        >
          <LogOut className="h-4 w-4" />
          <span>Logout</span>
        </button>
      </div>
    </aside>
  );
};
