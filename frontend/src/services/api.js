import axios from 'axios';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true // Permet l'envoi des cookies avec les requêtes
});

// Configuration d'axios pour inclure le token JWT dans les headers
api.interceptors.request.use(
    async (config) => {
        // Récupérer le token depuis le cookie
        const token = document.cookie
            .split('; ')
            .find(row => row.startsWith('jwt='))
            ?.split('=')[1];

        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// Gestion des erreurs de réponse
api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401) {
            // On ne redirige plus automatiquement
            console.log('Session expirée');
        }
        return Promise.reject(error);
    }
);

export const channelService = {
  getAllChannels: () => api.get('/api/channels'),
  getChannel: (id) => api.get(`/api/channels/${id}`),
  createChannel: (title, description, date, duration, ownerId) => api.post('/api/channels', { title, description, date, duration, ownerId }),
  updateChannel: (id, title, description, date, duration) => api.patch(`/api/channels/${id}`, { title, description, date, duration }),
  deleteChannel: (id) => api.delete(`/api/channels/${id}`),
};

export const userService = {
  getUserChannels: (userId) => api.get(`/api/users/${userId}/channels`),
  getCurrentUserChannels: async (user) => {
    if (!user) {
      throw new Error('User not authenticated');
    }
    const response = await api.get(`/api/users/${user.userId}/channels`);
    return response.data;
  }
};

export const messageService = {
  getMessages: (channelId) => api.get(`/api/channels/${channelId}/messages`),
  sendMessage: (channelId, message) => api.post(`/api/channels/${channelId}/messages`, message),
};

export const login = async (email, password) => {
    const response = await api.post('/api/auth/login', {
        email,
        password
    });
    return response.data;
};

export const register = async (userData) => {
    const response = await api.post('/api/auth/register', userData);
    return response.data;
};

export const logout = async () => {
    await api.post('/api/auth/logout');
    // On ne redirige plus ici
};

export const getCurrentUser = async () => {
    try {
        const response = await api.get('/api/auth/me');
        return response.data;
    } catch (error) {
        return null;
    }
};

export const isAuthenticated = async () => {
    try {
        await api.get('/api/auth/me');
        return true;
    } catch (error) {
        return false;
    }
};

export default api; 