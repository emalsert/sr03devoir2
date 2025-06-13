import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import { useAuth } from '../contexts/AuthContext';

class WebSocketService {
    constructor() {
        this.client = null;
        this.subscriptions = new Map();
        this.messageHandlers = new Map();
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
        this.reconnectDelay = 1000; // 1 seconde
        this.allowedImageTypes = ['image/jpeg', 'image/png', 'image/gif'];
        this.allowedDocumentTypes = ['application/pdf'];
        this.maxFileSize = 5 * 1024 * 1024; // 5MB
    }

    connect() {
        
         // Récupérer le token depuis le cookie
         const token = document.cookie
         .split('; ')
         .find(row => row.startsWith('jwt='))
         ?.split('=')[1];
        
        if (this.client && this.client.connected) {
            return Promise.resolve();
        }

        return new Promise((resolve, reject) => {
            const socket = new SockJS('http://localhost:8080/ws');
            this.client = new Client({
                webSocketFactory: () => socket,
                connectHeaders: {
                    'Authorization': `Bearer ${token}` // Envoyer le JWT
                },
                debug: (str) => {
                    console.log('STOMP Debug:', str);
                },
                reconnectDelay: this.reconnectDelay,
                heartbeatIncoming: 4000,
                heartbeatOutgoing: 4000,
                onConnect: () => {
                    console.log('Connected to WebSocket');
                    this.reconnectAttempts = 0;
                    resolve();
                },
                onDisconnect: () => {
                    console.log('Disconnected from WebSocket');
                    this.subscriptions.clear();
                    this.attemptReconnect();
                },
                onStompError: (frame) => {
                    console.error('STOMP error:', frame);
                    if (frame.headers.message.includes('401')) {
                        // Erreur d'authentification
                        console.error('Authentication error');
                        this.disconnect();
                        reject(new Error('Authentication failed'));
                    } else {
                        this.attemptReconnect();
                        reject(frame);
                    }
                },
                onWebSocketError: (event) => {
                    console.error('WebSocket error:', event);
                    this.attemptReconnect();
                    reject(event);
                }
            });

            this.client.activate();
        });
    }

    attemptReconnect() {
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++;
            console.log(`Attempting to reconnect (${this.reconnectAttempts}/${this.maxReconnectAttempts})...`);
            setTimeout(() => {
                this.connect().catch(error => {
                    console.error('Reconnection failed:', error);
                });
            }, this.reconnectDelay * this.reconnectAttempts);
        } else {
            console.error('Max reconnection attempts reached');
            this.disconnect();
        }
    }

    disconnect() {
        if (this.client) {
            this.client.deactivate();
            this.client = null;
            this.subscriptions.clear();
            this.messageHandlers.clear();
            this.reconnectAttempts = 0;
        }
    }

    subscribeToChannel(channelId, onMessage) {
        if (!this.client || !this.client.connected) {
            return this.connect().then(() => {
                this.subscribeToChannel(channelId, onMessage);
            }).catch(error => {
                console.error('Failed to connect and subscribe:', error);
                throw error;
            });
        }

        try {
            const subscription = this.client.subscribe(
                `/topic/chat/${channelId}`,
                (message) => {
                    try {
                        const parsedMessage = JSON.parse(message.body);
                        onMessage(parsedMessage);
                    } catch (error) {
                        console.error('Error parsing message:', error);
                        // En cas d'erreur de parsing, on envoie le message brut
                        onMessage(message.body);
                    }
                }
            );

            this.subscriptions.set(channelId, subscription);
            this.messageHandlers.set(channelId, onMessage);
        } catch (error) {
            console.error('Error subscribing to channel:', error);
            throw error;
        }
    }

    unsubscribeFromChannel(channelId) {
        const subscription = this.subscriptions.get(channelId);
        if (subscription) {
            subscription.unsubscribe();
            this.subscriptions.delete(channelId);
            this.messageHandlers.delete(channelId);
        }
    }

    sendMessage(channelId, content) {
        if (!this.client || !this.client.connected) {
            throw new Error('WebSocket not connected');
        }

        // Méthode plus robuste pour récupérer le cookie jwt
        const getCookie = (name) => {
            const value = `; ${document.cookie}`;
            const parts = value.split(`; ${name}=`);
            if (parts.length === 2) {
                return parts.pop().split(';').shift();
            }
            return null;
        };

        const token = getCookie('jwt');
        console.log('Token récupéré:', token); // Debug
        console.log('Tous les cookies:', document.cookie); // Debug

        return new Promise((resolve, reject) => {
            try {
                this.client.publish({
                    destination: `/app/chat/${channelId}/send`,
                    body: content,
                    headers: {
                        'Authorization': token ? `Bearer ${token}` : ''
                    }
                });
                resolve();
            } catch (error) {
                reject(error);
            }
        });
    }

    isConnected() {
        return this.client && this.client.connected;
    }

    async sendFile(channelId, file) {
        if (!this.client || !this.client.connected) {
            throw new Error('WebSocket not connected');
        }

        // Création d'un FormData pour l'envoi du fichier
        const formData = new FormData();
        formData.append('file', file);

        try {
            const response = await fetch(`http://localhost:8080/api/chat/${channelId}/file`, {
                method: 'POST',
                body: formData,
                credentials: 'include'
            });

            if (!response.ok) {
                throw new Error(`Erreur lors de l'envoi du fichier: ${response.statusText}`);
            }
        } catch (error) {
            console.error('Erreur lors de l\'envoi du fichier:', error);
            throw error;
        }
    }

    validateFile(file) {
        if (!this.allowedImageTypes.includes(file.type) && !this.allowedDocumentTypes.includes(file.type)) {
            throw new Error('Type de fichier non autorisé. Formats acceptés: JPEG, PNG, GIF, PDF');
        }

        if (file.size > this.maxFileSize) {
            throw new Error('Fichier trop volumineux. Taille maximale: 5MB');
        }

        return true;
    }

    // Méthode utilitaire pour afficher un fichier reçu
    displayFile(message) {
        if (message.type !== 'FILE') {
            return null;
        }

        const { fileName, fileType, content } = message;
        const byteCharacters = atob(content);
        const byteNumbers = new Array(byteCharacters.length);
        
        for (let i = 0; i < byteCharacters.length; i++) {
            byteNumbers[i] = byteCharacters.charCodeAt(i);
        }
        
        const byteArray = new Uint8Array(byteNumbers);
        const blob = new Blob([byteArray], { type: fileType });
        
        return {
            url: URL.createObjectURL(blob),
            fileName,
            fileType
        };
    }

    async getChannelUsers(channelId) {
        const response = await fetch(`http://localhost:8080/api/chat/${channelId}/users`, {
            method: 'GET',
            credentials: 'include'
        });

        return response.json();
    }
}

// Export une instance unique du service
export const websocketService = new WebSocketService(); 