import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Form, Button, Alert } from 'react-bootstrap';
import { channelService } from '../services/api';
import { useAuth } from '../contexts/AuthContext';

// Formulaire de création de canal (titre, description, date, durée)
const CanalForm = ({ onChannelCreated }) => {
    const [title, setTitle] = useState('');
    const [description, setDescription] = useState('');
    const [dateTime, setDateTime] = useState('');
    const [durationMinutes, setDurationMinutes] = useState('');
    const [error, setError] = useState('');
    const { user } = useAuth();
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();

        // Validation côté client
        const now = new Date();
        const selectedDate = new Date(dateTime);


        if (isNaN(selectedDate.getTime())) {
            setError("La date est invalide.");
            return;
        }
        if (selectedDate.getTime() <= now.getTime()) {
            setError("La date doit être dans le futur.");
            return;
        }
        if (!durationMinutes || isNaN(durationMinutes) || Number(durationMinutes) <= 0) {
            setError("La durée doit être un nombre positif.");
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
            if (onChannelCreated) onChannelCreated(true, response.data);
        } catch (error) {
            setError(error.response.data);
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
                    min={new Date().toISOString().slice(0, 16)}
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