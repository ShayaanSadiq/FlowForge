const API_BASE = import.meta.env.VITE_API_BASE_URL || '';

export function getAuthHeaders() {
  const token = localStorage.getItem('token');
  return token ? { Authorization: `Bearer ${token}` } : {};
}

export async function apiRequest(path, options = {}) {
  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...getAuthHeaders(),
      ...options.headers,
    },
  });

  if (!response.ok) {
    const error = await response.json().catch(() => ({ detail: response.statusText }));
    throw new Error(error.detail || 'Request failed');
  }

  if (response.status === 204) return null;
  return response.json();
}

export const authApi = {
  login: (data) => apiRequest('/api/auth/login', { method: 'POST', body: JSON.stringify(data) }),
  register: (data) => apiRequest('/api/auth/register', { method: 'POST', body: JSON.stringify(data) }),
};

export const jobsApi = {
  list: (page = 0, size = 20) => apiRequest(`/api/jobs?page=${page}&size=${size}`),
  get: (id) => apiRequest(`/api/jobs/${id}`),
  create: (data) => apiRequest('/api/jobs', { method: 'POST', body: JSON.stringify(data) }),
  retry: (id) => apiRequest(`/api/jobs/${id}/retry`, { method: 'POST' }),
};

export const statsApi = {
  get: () => apiRequest('/api/stats'),
};
