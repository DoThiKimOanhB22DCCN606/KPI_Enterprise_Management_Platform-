import { useEffect, useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { apiClient } from '../lib/apiClient';
import { auth } from '../lib/auth';
import toast from 'react-hot-toast';

export default function OAuthCallbackPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const [error, setError] = useState('');

  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const code = params.get('code');
    const state = params.get('state');

    if (!code) {
      setError('No authorization code found in URL');
      return;
    }

    const exchangeCode = async () => {
      try {
        const res = await apiClient.post('/v1/auth/oauth2/callback', { code, state });
        if (res.data && res.data.accessToken) {
          auth.setTokens(res.data.accessToken, res.data.refreshToken);
          toast.success('Successfully logged in');
          navigate('/dashboard');
        } else {
          setError('Invalid response from server');
        }
      } catch (err: any) {
        console.error('OAuth Callback Error:', err);
        setError(err.response?.data?.message || 'Authentication failed');
      }
    };

    exchangeCode();
  }, [location, navigate]);

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-950 text-white">
      {error ? (
        <div className="bg-red-900/20 border border-red-500/50 p-6 rounded-xl flex flex-col items-center">
          <h2 className="text-xl font-bold text-red-400 mb-2">Authentication Failed</h2>
          <p className="text-gray-300 mb-4">{error}</p>
          <button 
            onClick={() => navigate('/login')}
            className="px-4 py-2 bg-gray-800 hover:bg-gray-700 rounded-lg transition"
          >
            Back to Login
          </button>
        </div>
      ) : (
        <div className="flex flex-col items-center gap-4">
          <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-primary-500"></div>
          <h2 className="text-xl font-medium">Completing authentication...</h2>
          <p className="text-gray-400">Please wait while we log you in.</p>
        </div>
      )}
    </div>
  );
}
