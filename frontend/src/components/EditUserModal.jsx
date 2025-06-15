// Modal pour éditer les informations utilisateur (nom, email, avatar)

import React, { useState } from 'react';
import { Modal, Form, Button } from 'react-bootstrap';
import { userService } from '../services/api';
import { AdvancedImage } from '@cloudinary/react';
import { getCloudinaryImage, uploadToCloudinary } from '../services/cloudinaryService';
import axios from 'axios';

const EditUserModal = ({ show, handleClose, user, onUpdate }) => {
    const [firstName, setFirstName] = useState(user.firstName);
    const [lastName, setLastName] = useState(user.lastName);
    const [avatar, setAvatar] = useState(user.avatar);
    const [email, setEmail] = useState(user.email);

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            
            await userService.updateUser(user.userId, {
                firstName,
                lastName,
                email,
                avatar
            });
            onUpdate();
        } catch (error) {
            console.error('Error updating user avatar:', error);
        }
    };

    const handleAvatarChange = async (e) => {
        const file = e.target.files[0];
        if (file) {
            try {
                const publicId = await uploadToCloudinary(file);
                setAvatar(publicId);
            } catch (err) {
                console.error('Erreur upload Cloudinary', err);
            }
        }
    };

    const renderAvatar = () => {
        const publicId = avatar ? avatar : 'cat-chat_ua94gz';
        const img = getCloudinaryImage(publicId, 100, 100);
        return (
            <div className="d-flex justify-content-center">
                <AdvancedImage cldImg={img} style={{ width: 140, height: 140, borderRadius: '4%', objectFit: 'cover' }} />
            </div>
        );
    };

    return (
        <Modal show={show} onHide={handleClose}>
            <Modal.Header closeButton>
                <Modal.Title>Modifier les informations de l'utilisateur</Modal.Title>
            </Modal.Header>
            <Modal.Body>
                <Form onSubmit={handleSubmit}>
                    <div className="d-flex align-items-center">
                        <div>
                            <Form.Group controlId="firstName">
                                <Form.Label>Prénom</Form.Label>
                                <Form.Control type="text" value={firstName} onChange={(e) => setFirstName(e.target.value)} />
                            </Form.Group>
                            <Form.Group controlId="lastName">
                                <Form.Label>Nom</Form.Label>
                                <Form.Control type="text" value={lastName} onChange={(e) => setLastName(e.target.value)} />
                            </Form.Group>
                        </div>
                        <div className="m-auto">
                            {renderAvatar()}
                        </div>
                    </div>
                    
                    
                    <Form.Group controlId="avatar">
                        <Form.Label>Avatar</Form.Label>
                        <Form.Control
                            type="file"
                            onChange={handleAvatarChange}
                        />
                        <Form.Text className="text-muted">
                            Vous pouvez modifier votre avatar en téléchargeant un fichier image.
                        </Form.Text>
                    </Form.Group>
                    <Form.Group controlId="email">
                        <Form.Label>Email</Form.Label>
                        <Form.Control type="email" value={email} onChange={(e) => setEmail(e.target.value)} />
                    </Form.Group>
                    <Button className="mt-3" variant="primary" type="submit">
                        Enregistrer
                    </Button>
                </Form>
            </Modal.Body>
        </Modal>
    );
};

export default EditUserModal;