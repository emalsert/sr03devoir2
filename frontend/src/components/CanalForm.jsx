import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Form, Button, Alert } from 'react-bootstrap';
import { channelService } from '../services/api';
import { useAuth } from '../contexts/AuthContext';

const CanalForm = () => {
    const [title, setTitle] = useState('');
    const [description, setDescription] = useState('');
    const [dateTime, setDateTime] = useState('');
    const [durationMinutes, setDurationMinutes] = useState('');
    const [error, setError] = useState('');
    const { user } = useAuth();
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!user) {
            setError('Vous devez être connecté pour créer un canal');
            return;
        }

        try {
            const response = await channelService.createChannel(
                title, 
                description, 
                dateTime, 
                durationMinutes, 
                user.userId
            );
            navigate('/channels');
        } catch (error) {
            // Gestion améliorée des erreurs
            let errorMessage = 'Une erreur est survenue';
            
            if (error.response) {
                // L'erreur vient du serveur
                if (typeof error.response.data === 'string') {
                    errorMessage = error.response.data;
                } else if (error.response.data.message) {
                    errorMessage = error.response.data.message;
                } else if (error.response.data.error) {
                    errorMessage = error.response.data.error;
                }
            } else if (error.message) {
                // L'erreur vient du client
                errorMessage = error.message;
            }
            
            setError(errorMessage);
            console.error('Erreur détaillée:', error);
        }
    }

    return (
        <Form onSubmit={handleSubmit}>
            {error && (
                <Alert variant="danger" onClose={() => setError('')} dismissible>
                    {error}
                </Alert>
            )}
            <Form.Group className="mb-3">
                <Form.Label>Titre</Form.Label>
                <Form.Control
                    type="text"
                    value={title}
                    onChange={(e) => setTitle(e.target.value)}
                    required
                />
            </Form.Group>
            <Form.Group className="mb-3">
                <Form.Label>Description</Form.Label>
                <Form.Control
                    as="textarea"
                    rows={3}
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                    required
                />
            </Form.Group>
            <Form.Group className="mb-3">
                <Form.Label>Date et heure</Form.Label>
                <Form.Control
                    type="datetime-local"
                    value={dateTime}
                    onChange={(e) => setDateTime(e.target.value)}
                    required
                />
            </Form.Group>
            <Form.Group className="mb-3">
                <Form.Label>Durée (en minutes)</Form.Label>
                <Form.Control
                    type="number"
                    min="1"
                    value={durationMinutes}
                    onChange={(e) => setDurationMinutes(e.target.value)}
                    required
                />
            </Form.Group>
            <Button variant="primary" type="submit" className="w-100">
                Créer
            </Button>
        </Form>
    )
}

export default CanalForm;