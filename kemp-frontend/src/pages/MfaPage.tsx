import { useState, useRef, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { apiClient } from '../lib/apiClient';
import clsx from 'clsx';
import { KeyRound } from 'lucide-react';

export default function MfaPage() {
  const [code, setCode] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const inputRef = useRef<HTMLInputElement>(null);
  
  const tempToken = location.state?.tempToken;

  useEffect(() => {
    if (!tempToken) {
      navigate('/login');
    }
    inputRef.current?.focus();
  }, [tempToken, navigate]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const response = await apiClient.post('/v1/auth/mfa/verify', { code, tempToken });
      if (response.data.accessToken) {
        localStorage.setItem('accessToken', response.data.accessToken);
        localStorage.setItem('refreshToken', response.data.refreshToken);
        navigate('/dashboard');
      }
    } catch (err: any) {
      setError(err.response?.data?.message || 'Invalid verification code.');
    } finally {
      setLoading(false);
    }
  };

  const handleCodeChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value.replace(/[^0-9]/g, '');
    if (value.length <= 6) {
      setCode(value);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-900 p-4">
      <div className="max-w-md w-full space-y-8 bg-gray-800 p-8 rounded-xl shadow-2xl border border-gray-700">
        <div className="text-center">
          <div className="mx-auto h-12 w-12 bg-primary-900/50 rounded-full flex items-center justify-center mb-4">
            <KeyRound className="h-6 w-6 text-primary-500" />
          </div>
          <h2 className="text-2xl font-bold text-white tracking-tight">Two-Factor Authentication</h2>
          <p className="mt-2 text-sm text-gray-400">Enter the 6-digit code from your authenticator app</p>
        </div>
        
        {error && (
          <div className="bg-danger/10 border border-danger/20 text-danger px-4 py-3 rounded-lg text-sm text-center">
            {error}
          </div>
        )}

        <form className="mt-8 space-y-6" onSubmit={handleSubmit}>
          <div>
            <label htmlFor="mfa-code" className="sr-only">Verification Code</label>
            <input
              ref={inputRef}
              id="mfa-code"
              type="text"
              autoComplete="one-time-code"
              required
              value={code}
              onChange={handleCodeChange}
              className="block w-full bg-gray-900 border border-gray-700 rounded-lg py-3 text-gray-100 text-center text-2xl tracking-[0.5em] placeholder-gray-600 focus:ring-2 focus:ring-primary-500 focus:border-primary-500 transition-colors"
              placeholder="000000"
            />
          </div>

          <button
            type="submit"
            disabled={loading || code.length !== 6}
            className={clsx(
              "w-full flex justify-center py-2.5 px-4 border border-transparent rounded-lg shadow-sm text-sm font-medium text-white bg-primary-500 hover:bg-primary-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500 transition-colors focus:ring-offset-gray-900",
              (loading || code.length !== 6) && "opacity-70 cursor-not-allowed"
            )}
          >
            {loading ? 'Verifying...' : 'Verify'}
          </button>
        </form>
      </div>
    </div>
  );
}
