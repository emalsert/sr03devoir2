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

// Service pour les canaux
export const channelService = {
    getAllChannels: () => api.get('/api/channels'),
    getChannel: (id) => api.get(`/api/channels/${id}`),
    createChannel: (title, description, date, durationMinutes, ownerId) => {
        // Formatage de la date au format YYYY-MM-DDThh:mm:ss
        const formattedDate = new Date(date).toISOString().slice(0, 19);
        return api.post('/api/channels/create', null, {
            params: {
                title,
                description,
                date: formattedDate,
                durationMinutes: parseInt(durationMinutes),
                ownerId: parseInt(ownerId)
            }
        });
    },
    updateChannel: (id, title, description, date, durationMinutes) => {
        // Formatage de la date au format YYYY-MM-DDThh:mm:ss
        const formattedDate = new Date(date).toISOString().slice(0, 19);
        return api.patch(`/api/channels/${id}`, null, {
            params: {
                title,
                description,
                date: formattedDate,
                durationMinutes: parseInt(durationMinutes)
            }
        });
    },
    deleteChannel: async (channelId) => {
        const response = await api.delete(`/api/channels/${channelId}`);
        return response.data;
    },
};

// Service pour les utilisateurs
export const userService = {
    getUserChannels: (userId) => api.get(`/api/users/${userId}/channels`),
    getCurrentUserChannels: async (user) => {
        if (!user) {
            throw new Error('User not authenticated');
        }
        const response = await api.get(`/api/users/${user.userId}/channels`);
        return response.data;
    },
    getAllUsers: async () => {
        const response = await api.get('/api/users');
        return response.data;
    },
};

// Service pour les messages
export const messageService = {
    sendMessage: (channelId, message) => api.post(`/api/channels/${channelId}/messages`, message),
};

// Service pour les invitations
export const invitationService = {
    // Récupérer les invitations de l'utilisateur
    getUserInvitations: async (user) => {
        if (!user) {
            throw new Error('User not authenticated');
        }
        const response = await api.get(`/api/invitations/user/${user.userId}`);
        return response.data;
    },

    // Accepter une invitation
    acceptInvitation: async (invitationId, channelId) => {
        const response = await api.post(`/api/invitations/${invitationId}/accept`, null, {
            params: {
                channelId: channelId
            }
        });
        console.log(response);
        return response.data;
    },

    // Rejeter une invitation
    declineInvitation: async (invitationId, channelId) => {
        const response = await api.post(`/api/invitations/${invitationId}/decline`, null, {
            params: {
                channelId: channelId
            }
        });
        return response.data;
    },

    sendInvitation: async (userId, channelId) => {
        const response = await api.post('/api/invitations/invite', null, {
            params: {
                channelId: channelId,
                userId: userId,
            }
        });
        return response.data;
    }
};

// Services d'authentification
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
