import React, { useState, useEffect } from 'react';
import { Modal, Form, Button, Alert } from 'react-bootstrap';
import { channelService } from '../services/api';

// Modal pour éditer les informations d'un canal (titre, description, date, durée)

function toDatetimeLocal(date) {
    const d = new Date(date);
    d.setMinutes(d.getMinutes() - d.getTimezoneOffset());
    return d.toISOString().slice(0, 16);
}

const EditChannelModal = ({ show, handleClose, channel, onUpdate }) => {
    const [title, setTitle] = useState('');
    const [description, setDescription] = useState('');
    const [dateTime, setDateTime] = useState('');
    const [durationMinutes, setDurationMinutes] = useState('');
    const [error, setError] = useState('');

    useEffect(() => {
        if (channel) {
            setTitle(channel.title);
            setDescription(channel.description);
            setDateTime(toDatetimeLocal(channel.date));
            setDurationMinutes(channel.durationMinutes);
        }
    }, [channel]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            await channelService.updateChannel(
                channel.channelId,
                title,
                description,
                dateTime,
                durationMinutes
            );
            onUpdate();
            handleClose();
        } catch (error) {
            let errorMessage = 'Une erreur est survenue';
            if (error.response?.data) {
                errorMessage = error.response.data;
            }
            setError(errorMessage);
        }
    };

    return (
        <Modal show={show} onHide={handleClose}>
            <Modal.Header closeButton>
                <Modal.Title>Modifier le canal</Modal.Title>
            </Modal.Header>
            <Modal.Body>
                {error && (
                    <Alert variant="danger" onClose={() => setError('')} dismissible>
                        {error}
                    </Alert>
                )}
                <Form onSubmit={handleSubmit}>
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
                    <div className="d-flex justify-content-end gap-2">
                        <Button variant="secondary" onClick={handleClose}>
                            Annuler
                        </Button>
                        <Button variant="primary" type="submit">
                            Enregistrer
                        </Button>
                    </div>
                </Form>
            </Modal.Body>
        </Modal>
    );
};

export default EditChannelModal; 