import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import MfaPage from './pages/MfaPage';
import DashboardPage from './pages/DashboardPage';
import KpiListPage from './pages/KpiListPage';
import KpiCreateForm from './pages/KpiCreateForm';
import KpiProposalForm from './pages/KpiProposalForm';
import KpiDetailPage from './pages/KpiDetailPage';
import GoalTreePage from './pages/GoalTreePage';
import LeaderboardPage from './pages/LeaderboardPage';
import ReportsPage from './pages/ReportsPage';
import NotificationsPage from './pages/NotificationsPage';
import AdminUsersPage from './pages/AdminUsersPage';
import AdminRolesPage from './pages/AdminRolesPage';
import AuditLogPage from './pages/AuditLogPage';
import DashboardBuilderPage from './pages/DashboardBuilderPage';
import OAuthCallbackPage from './pages/OAuthCallbackPage';
import DynamicDashboardPage from './pages/DynamicDashboardPage';
import OrganizationPage from './pages/OrganizationPage';
import DashboardListPage from './pages/DashboardListPage';
import CyclesPage from './pages/CyclesPage';
import KpiTemplatesPage from './pages/KpiTemplatesPage';
import { auth } from './lib/auth';
import { Layout } from './components/Layout';
import { Toaster } from 'react-hot-toast';

// Helper for protected routes
const ProtectedRoute = ({ children, requiredRoles }: { children: React.ReactNode, requiredRoles?: string[] }) => {
  const user = auth.getCurrentUser();
  
  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (requiredRoles && requiredRoles.length > 0) {
    const hasRole = user.roles?.some((role: string) => requiredRoles.includes(role));
    if (!hasRole) {
      return <Navigate to="/dashboard" replace />;
    }
  }

  return <>{children}</>;
};

// Placeholder pages for incomplete routes
/*
const Placeholder = ({ title }: { title: string }) => (
  <div className="min-h-screen flex items-center justify-center bg-gray-900 text-white">
    <h1 className="text-2xl font-bold">{title} Page (Coming Soon)</h1>
  </div>
);
*/

export default function App() {
  return (
    <Router>
      <Toaster position="top-right" toastOptions={{
        style: {
          background: '#1f2937',
          color: '#fff',
          border: '1px solid #374151',
        },
      }} />
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/mfa" element={<MfaPage />} />
        <Route path="/oauth/callback" element={<OAuthCallbackPage />} />
        
        <Route path="/" element={<Navigate to="/dashboard" replace />} />
        
        <Route path="/dashboard" element={
          <ProtectedRoute>
            <Layout>
              <DashboardPage />
            </Layout>
          </ProtectedRoute>
        } />
        
        <Route path="/kpis" element={
          <ProtectedRoute>
            <Layout>
              <KpiListPage />
            </Layout>
          </ProtectedRoute>
        } />
        
        <Route path="/kpis/new" element={
          <ProtectedRoute>
            <Layout>
              <KpiCreateForm />
            </Layout>
          </ProtectedRoute>
        } />
        
        <Route path="/kpis/propose" element={
          <ProtectedRoute>
            <Layout>
              <KpiProposalForm />
            </Layout>
          </ProtectedRoute>
        } />
        
        <Route path="/kpis/:id" element={
          <ProtectedRoute>
            <Layout>
              <KpiDetailPage />
            </Layout>
          </ProtectedRoute>
        } />
        
        <Route path="/goals" element={
          <ProtectedRoute>
            <Layout>
              <GoalTreePage />
            </Layout>
          </ProtectedRoute>
        } />
        
        <Route path="/leaderboard" element={
          <ProtectedRoute>
            <Layout>
              <LeaderboardPage />
            </Layout>
          </ProtectedRoute>
        } />
        
        <Route path="/reports" element={
          <ProtectedRoute>
            <Layout>
              <ReportsPage />
            </Layout>
          </ProtectedRoute>
        } />
        
        <Route path="/notifications" element={
          <ProtectedRoute>
            <Layout>
              <NotificationsPage />
            </Layout>
          </ProtectedRoute>
        } />
        
        <Route path="/admin/users" element={
          <ProtectedRoute requiredRoles={['TENANT_ADMIN', 'HR_ADMIN']}>
            <Layout>
              <AdminUsersPage />
            </Layout>
          </ProtectedRoute>
        } />
        
        <Route path="/admin/roles" element={
          <ProtectedRoute requiredRoles={['TENANT_ADMIN', 'HR_ADMIN']}>
            <Layout>
              <AdminRolesPage />
            </Layout>
          </ProtectedRoute>
        } />
        
        <Route path="/admin/organizations" element={
          <ProtectedRoute requiredRoles={['TENANT_ADMIN', 'HR_ADMIN']}>
            <Layout>
              <OrganizationPage />
            </Layout>
          </ProtectedRoute>
        } />
        
        <Route path="/admin/cycles" element={
          <ProtectedRoute requiredRoles={['TENANT_ADMIN', 'HR_ADMIN']}>
            <Layout>
              <CyclesPage />
            </Layout>
          </ProtectedRoute>
        } />
        
        <Route path="/admin/kpi-templates" element={
          <ProtectedRoute requiredRoles={['TENANT_ADMIN', 'HR_ADMIN']}>
            <Layout>
              <KpiTemplatesPage />
            </Layout>
          </ProtectedRoute>
        } />
        
        <Route path="/admin/audit" element={
          <ProtectedRoute requiredRoles={['TENANT_ADMIN']}>
            <Layout>
              <AuditLogPage />
            </Layout>
          </ProtectedRoute>
        } />

        <Route path="/dashboard/builder" element={
          <ProtectedRoute requiredRoles={['TENANT_ADMIN']}>
            <Layout>
              <DashboardBuilderPage />
            </Layout>
          </ProtectedRoute>
        } />

        <Route path="/dashboards" element={
          <ProtectedRoute>
            <Layout>
              <DashboardListPage />
            </Layout>
          </ProtectedRoute>
        } />

        <Route path="/dashboards/view/:id" element={
          <ProtectedRoute>
            <Layout>
              <DynamicDashboardPage />
            </Layout>
          </ProtectedRoute>
        } />
      </Routes>
    </Router>
  );
}

