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

function normalizePageResponse(page) {
  if (!page || !page.page) {
    return page;
  }

  return {
    content: page.content,
    totalElements: page.page.totalElements,
    totalPages: page.page.totalPages,
    number: page.page.number,
    size: page.page.size,
  };
}

export const authApi = {
  login: (data) => apiRequest('/api/auth/login', { method: 'POST', body: JSON.stringify(data) }),
  register: (data) => apiRequest('/api/auth/register', { method: 'POST', body: JSON.stringify(data) }),
};

export const jobsApi = {
  list: ({ page = 0, size = 20, status, type, sort = 'createdAt,desc' } = {}) => {
    const params = new URLSearchParams({
      page: String(page),
      size: String(size),
      sort,
    });
    if (status && status !== 'ALL') {
      params.set('status', status);
    }
    if (type && type !== 'ALL') {
      params.set('type', type);
    }
    return apiRequest(`/api/jobs?${params}`).then(normalizePageResponse);
  },
  get: (id) => apiRequest(`/api/jobs/${id}`),
  create: (data) => apiRequest('/api/jobs', { method: 'POST', body: JSON.stringify(data) }),
  retry: (id) => apiRequest(`/api/jobs/${id}/retry`, { method: 'POST' }),
};

export const statsApi = {
  get: () => apiRequest('/api/stats'),
};
