import React, { useState, useEffect, useRef } from 'react';
import { MessageSquare, X, Send, Bot, User, Loader2 } from 'lucide-react';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer } from 'recharts';
import { aiApi, type AiMessage } from '../api/aiApi';
import { DataTable, type Column } from './DataTable';

export const AiChatWidget: React.FC = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [messages, setMessages] = useState<AiMessage[]>([]);
  const [input, setInput] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [conversationId, setConversationId] = useState<string | null>(null);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    if (isOpen) {
      scrollToBottom();
    }
  }, [messages, isOpen, isLoading]);

  useEffect(() => {
    if (isOpen && messages.length === 0 && !conversationId) {
      loadHistory();
    }
  }, [isOpen]);

  const loadHistory = async () => {
    try {
      setIsLoading(true);
      const convs = await aiApi.getConversations();
      if (convs && convs.length > 0) {
        const latestId = convs[0].id;
        setConversationId(latestId);
        const msgs = await aiApi.getMessages(latestId);
        setMessages(msgs);
      }
    } catch (err) {
      console.error('Failed to load conversation history', err);
    } finally {
      setIsLoading(false);
    }
  };

  const handleSend = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!input.trim() || isLoading) return;

    const userMessage: AiMessage = { role: 'USER', content: input };
    setMessages(prev => [...prev, userMessage]);
    setInput('');
    setIsLoading(true);

    try {
      const response = await aiApi.queryAi({
        prompt: userMessage.content,
        conversationId: conversationId
      });
      
      setConversationId(response.conversationId);
      
      const aiMessage: AiMessage = {
        role: 'AI',
        content: response.answer,
        sqlQuery: response.sqlQuery,
        rawData: response.rawData
      };
      
      setMessages(prev => [...prev, aiMessage]);
    } catch (err) {
      console.error('Failed to send query', err);
      const errorMsg: AiMessage = { role: 'AI', content: 'An error occurred while processing your request. Please check your API key.' };
      setMessages(prev => [...prev, errorMsg]);
    } finally {
      setIsLoading(false);
    }
  };

  const renderData = (data: any) => {
    if (!data || !Array.isArray(data) || data.length === 0) return null;

    const keys = Object.keys(data[0]);
    if (keys.length === 0) return null;

    const numericKey = keys.find(key => typeof data[0][key] === 'number');
    const stringKey = keys.find(key => typeof data[0][key] === 'string') || keys[0];

    if (numericKey) {
      return (
        <div className="h-64 w-full mt-4 bg-gray-900 rounded-md p-2">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={data} margin={{ top: 10, right: 10, left: 0, bottom: 20 }}>
              <XAxis dataKey={stringKey} tick={{ fill: '#9ca3af', fontSize: 12 }} angle={-45} textAnchor="end" />
              <YAxis tick={{ fill: '#9ca3af', fontSize: 12 }} />
              <Tooltip contentStyle={{ backgroundColor: '#1f2937', border: 'none', color: '#f3f4f6' }} />
              <Bar dataKey={numericKey} fill="#3b82f6" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      );
    }

    // Fallback to DataTable
    const columns: Column<any>[] = keys.map(k => ({ header: k, accessor: k }));
    return (
      <div className="mt-4">
        <DataTable columns={columns} data={data} />
      </div>
    );
  };

  return (
    <div className="fixed bottom-6 right-6 z-50">
      {isOpen ? (
        <div className="bg-gray-800/90 backdrop-blur-md border border-gray-700 rounded-2xl shadow-2xl flex flex-col w-[400px] h-[600px] max-h-[80vh]">
          {/* Header */}
          <div className="flex items-center justify-between p-4 border-b border-gray-700 bg-gray-800/50 rounded-t-2xl">
            <div className="flex items-center gap-2">
              <Bot className="text-blue-400 w-5 h-5" />
              <h3 className="text-gray-100 font-semibold">AI Assistant</h3>
            </div>
            <button onClick={() => setIsOpen(false)} className="text-gray-400 hover:text-gray-200 transition">
              <X className="w-5 h-5" />
            </button>
          </div>

          {/* Messages */}
          <div className="flex-1 overflow-y-auto p-4 space-y-4">
            {messages.length === 0 && !isLoading && (
              <div className="text-center text-gray-400 mt-10">
                <p>Hello! I am your AI Assistant.</p>
                <p className="text-sm mt-2">Ask me anything about your KPIs and data.</p>
              </div>
            )}
            
            {messages.map((msg, idx) => (
              <div key={idx} className={`flex ${msg.role === 'USER' ? 'justify-end' : 'justify-start'}`}>
                <div className={`flex gap-3 max-w-[90%] ${msg.role === 'USER' ? 'flex-row-reverse' : 'flex-row'}`}>
                  <div className={`w-8 h-8 rounded-full flex items-center justify-center shrink-0 ${msg.role === 'USER' ? 'bg-blue-600' : 'bg-gray-700'}`}>
                    {msg.role === 'USER' ? <User className="w-5 h-5 text-white" /> : <Bot className="w-5 h-5 text-blue-400" />}
                  </div>
                  <div className={`p-3 rounded-2xl overflow-hidden ${msg.role === 'USER' ? 'bg-blue-600 text-white rounded-tr-none' : 'bg-gray-700/80 text-gray-100 rounded-tl-none'}`}>
                    <p className="whitespace-pre-wrap text-sm">{msg.content}</p>
                    {msg.role === 'AI' && msg.rawData && renderData(msg.rawData)}
                  </div>
                </div>
              </div>
            ))}
            
            {isLoading && (
              <div className="flex justify-start">
                <div className="flex gap-3 max-w-[85%] flex-row">
                  <div className="w-8 h-8 rounded-full flex items-center justify-center shrink-0 bg-gray-700">
                    <Bot className="w-5 h-5 text-blue-400" />
                  </div>
                  <div className="p-3 rounded-2xl bg-gray-700/80 text-gray-100 rounded-tl-none flex items-center">
                    <Loader2 className="w-5 h-5 animate-spin text-blue-400" />
                  </div>
                </div>
              </div>
            )}
            <div ref={messagesEndRef} />
          </div>

          {/* Input */}
          <form onSubmit={handleSend} className="p-3 border-t border-gray-700 bg-gray-800/50 rounded-b-2xl flex gap-2">
            <input
              type="text"
              value={input}
              onChange={(e) => setInput(e.target.value)}
              placeholder="Ask about your KPIs..."
              disabled={isLoading}
              className="flex-1 bg-gray-900 border border-gray-700 rounded-xl px-4 py-2 text-sm text-gray-100 focus:outline-none focus:border-blue-500 disabled:opacity-50"
            />
            <button
              type="submit"
              disabled={!input.trim() || isLoading}
              className="bg-blue-600 hover:bg-blue-500 text-white p-2 rounded-xl transition disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <Send className="w-5 h-5" />
            </button>
          </form>
        </div>
      ) : (
        <button
          onClick={() => setIsOpen(true)}
          className="bg-blue-600 hover:bg-blue-500 text-white p-4 rounded-full shadow-xl shadow-blue-900/20 transition-transform hover:scale-105 flex items-center justify-center"
        >
          <MessageSquare className="w-6 h-6" />
        </button>
      )}
    </div>
  );
};
