import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '../lib/apiClient';
import { PageHeader } from '../components/PageHeader';
import { Bell, CheckCircle } from 'lucide-react';
import clsx from 'clsx';

interface Notification {
  id: string;
  title: string;
  message: string;
  isRead: boolean;
  createdAt: string;
}

export default function NotificationsPage() {
  const queryClient = useQueryClient();

  const { data: notifications } = useQuery({
    queryKey: ['notifications'],
    queryFn: async () => (await apiClient.get('/v1/notifications/inbox')).data as Notification[]
  });

  const markRead = useMutation({
    mutationFn: async (id: string) => apiClient.post(`/v1/notifications/${id}/read`),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['notifications'] })
  });

  const markAllRead = useMutation({
    mutationFn: async () => {
      if (!notifications) return;
      const unread = notifications.filter(n => !n.isRead);
      await Promise.all(unread.map(n => apiClient.post(`/v1/notifications/${n.id}/read`)));
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['notifications'] })
  });

  const unreadCount = notifications?.filter(n => !n.isRead).length || 0;

  return (
    <div className="p-8 max-w-4xl mx-auto">
      <PageHeader 
        title="Notifications" 
        action={
          <div className="flex items-center gap-4">
            <span className="bg-primary-500/20 text-primary-400 px-3 py-1 rounded-full text-sm font-medium">
              {unreadCount} Unread
            </span>
            <button 
              onClick={() => markAllRead.mutate()}
              disabled={unreadCount === 0 || markAllRead.isPending}
              className="text-gray-400 hover:text-white text-sm font-medium disabled:opacity-50"
            >
              Mark all as read
            </button>
          </div>
        }
      />

      <div className="space-y-4">
        {notifications?.map(notif => (
          <div 
            key={notif.id} 
            className={clsx(
              "p-4 rounded-xl border transition flex gap-4 items-start",
              notif.isRead ? "bg-gray-800 border-gray-700 opacity-70" : "bg-gray-800 border-primary-500/30 shadow-lg shadow-primary-500/5"
            )}
          >
            <div className={clsx("p-2 rounded-full", notif.isRead ? "bg-gray-700 text-gray-400" : "bg-primary-500/20 text-primary-400")}>
              <Bell size={20} />
            </div>
            <div className="flex-1">
              <h4 className={clsx("font-semibold", notif.isRead ? "text-gray-300" : "text-white")}>{notif.title}</h4>
              <p className="text-gray-400 text-sm mt-1">{notif.message}</p>
              <div className="text-xs text-gray-500 mt-2">{new Date(notif.createdAt).toLocaleString()}</div>
            </div>
            {!notif.isRead && (
              <button 
                onClick={() => markRead.mutate(notif.id)}
                className="text-gray-400 hover:text-success"
                title="Mark as read"
              >
                <CheckCircle size={20} />
              </button>
            )}
          </div>
        ))}

        {(!notifications || notifications.length === 0) && (
          <div className="text-center p-8 text-gray-500">You have no notifications.</div>
        )}
      </div>
    </div>
  );
}
