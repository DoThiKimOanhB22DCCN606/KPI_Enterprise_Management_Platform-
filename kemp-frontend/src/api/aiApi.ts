import { apiClient } from '../lib/apiClient';

export interface AiMessage {
  id?: string;
  role: 'USER' | 'AI';
  content: string;
  sqlQuery?: string;
  rawData?: any;
  createdAt?: string;
}

export interface AiConversation {
  id: string;
  title: string;
  createdAt: string;
}

export interface AiQueryRequest {
  prompt: string;
  conversationId?: string | null;
}

export interface AiQueryResponse {
  answer: string;
  sqlQuery: string;
  rawData: any;
  conversationId: string;
}

export const aiApi = {
  getConversations: async (): Promise<AiConversation[]> => {
    const { data } = await apiClient.get('/v1/ai/conversations');
    return data;
  },

  getMessages: async (conversationId: string): Promise<AiMessage[]> => {
    const { data } = await apiClient.get(`/v1/ai/conversations/${conversationId}/messages`);
    return data;
  },

  queryAi: async (request: AiQueryRequest): Promise<AiQueryResponse> => {
    const { data } = await apiClient.post('/v1/ai/query', request);
    return data;
  }
};
