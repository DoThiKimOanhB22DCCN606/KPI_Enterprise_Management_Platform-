import React from 'react';
import { Sidebar } from './Sidebar';
import { AiChatWidget } from './AiChatWidget';

interface LayoutProps {
  children: React.ReactNode;
}

export const Layout: React.FC<LayoutProps> = ({ children }) => {

  return (
    <div className="flex min-h-screen bg-gray-900 text-gray-100 font-sans antialiased">
      {/* Sidebar */}
      <Sidebar />

      {/* Main Content Area */}
      <div className="flex-1 flex flex-col h-screen overflow-hidden">
        {/* Main Content Scroll Container */}
        <main className="flex-1 overflow-y-auto bg-gray-950 p-6 md:p-8">
          <div className="max-w-7xl mx-auto">
            {children}
          </div>
        </main>
      </div>
      
      {/* Global AI Chat Widget */}
      <AiChatWidget />
    </div>
  );
};
