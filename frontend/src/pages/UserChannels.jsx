import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Button, Alert, Spinner } from 'react-bootstrap';
import { userService, channelService, invitationService } from '../services/api'; // Assurez-vous que invitationService est bien importé
import { useAuth } from '../contexts/AuthContext';
import CanalForm from '../components/CanalForm';
import EditChannelModal from '../components/EditChannelModal';
import { useCallback } from 'react';

const UserChannels = () => {
  const [channels, setChannels] = useState([]);
  const [invitations, setInvitations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedChannel, setSelectedChannel] = useState(null);
  const [showEditModal, setShowEditModal] = useState(false);
  const { user } = useAuth();

  // Mémorisation des fonctions
  const loadUserChannels = useCallback(async () => {
    try {
      setLoading(true);
      const channelsData = await userService.getCurrentUserChannels(user);
      setChannels(channelsData);
      setError(null);
    } catch (err) {
      setError('Erreur lors du chargement des canaux');
      console.error('Error loading user channels:', err);
    } finally {
      setLoading(false);
    }
  }, [user]);

  const loadUserInvitations = useCallback(async () => {
    try {
      setLoading(true);
      const invitationsData = await invitationService.getUserInvitations(user);
      setInvitations(invitationsData);
      setError(null);
    } catch (err) {
      setError('Erreur lors du chargement des invitations');
      console.error('Error loading user invitations:', err);
    } finally {
      setLoading(false);
    }
  }, [user]);

// Utilisation de useEffect
  useEffect(() => {
    if (user) {
      loadUserChannels();
      loadUserInvitations();
    }
  }, [user, loadUserChannels, loadUserInvitations]); // Ici on peut se permettre d'ajouter loadUserChannels et loadUserInvitations car elles sont mémorisées

  // Gérer l'édition d'un canal
  const handleEdit = (channel) => {
    setSelectedChannel(channel);
    setShowEditModal(true);
  };

  // Gérer la suppression d'un canal
  const handleDelete = async (channel) => {
    if (window.confirm('Êtes-vous sûr de vouloir supprimer ce canal ?')) {
      try {
        await channelService.deleteChannel(channel.channelId);
        await loadUserChannels();
      } catch (err) {
        setError('Erreur lors de la suppression du canal');
        console.error('Error deleting channel:', err);
      }
    }
  };

  // Gérer l'acceptation d'une invitation
  const handleAccept = async (invitation) => {
    try {
      await invitationService.acceptInvitation(invitation.invitationId, invitation.channel.channelId);
      await loadUserInvitations(); // Recharger les invitations
    } catch (err) {
      setError('Erreur lors de l\'acceptation de l\'invitation');
      console.error('Error accepting invitation:', err);
    }
  };

  // Gérer le rejet d'une invitation
  const handleReject = async (invitation) => {
    try {
      await invitationService.declineInvitation(invitation.invitationId, invitation.channel.channelId);
      await loadUserInvitations(); // Recharger les invitations
    } catch (err) {
      setError('Erreur lors du rejet de l\'invitation');
      console.error('Error rejecting invitation:', err);
    }
  };

  if (loading) {
    return (
        <div className="d-flex justify-content-center align-items-center" style={{ height: '50vh' }}>
          <Spinner animation="border" role="status">
            <span className="visually-hidden">Chargement...</span>
          </Spinner>
        </div>
    );
  }

  if (error) {
    return (
        <Container>
          <Alert variant="danger">{error}</Alert>
        </Container>
    );
  }

  return (
      <Container className="mt-4">
        <h2 className="mb-4">Mes Canaux</h2>
        {channels.length === 0 ? (
            <Alert variant="info">Aucun canal trouvé</Alert>
        ) : (
            <Row>
              {channels.map((channel) => (
                  <Col key={channel.channelId} md={6} lg={4} className="mb-4">
                    <Card>
                      <Card.Body>
                        <Card.Title>{channel.title}</Card.Title>
                        <Card.Text>{channel.description}</Card.Text>
                        <div className="d-flex justify-content-between text-muted">
                          <small>Date: {new Date(channel.date).toLocaleDateString()}</small>
                          <small>Durée: {channel.durationMinutes} minutes</small>
                        </div>
                        <div className="d-flex gap-2">
                          <Button
                              variant="primary"
                              size="sm"
                              onClick={() => handleEdit(channel)}
                          >
                            Modifier
                          </Button>
                          <Button
                              variant="danger"
                              size="sm"
                              onClick={() => handleDelete(channel)}
                          >
                            Supprimer
                          </Button>
                        </div>
                      </Card.Body>
                    </Card>
                  </Col>
              ))}
            </Row>
        )}

        {/* Mes Invitations Section */}
        <h2 className="mb-4">Mes Invitations</h2>
        {invitations.length === 0 ? (
            <Alert variant="info">Aucune invitation reçue</Alert>
        ) : (
            <Row>
              {invitations.map((invitation) => (
                  <Col key={invitation.invitationId} md={6} lg={4} className="mb-4">
                    <Card>
                      <Card.Body>
                        <Card.Title>Channel ID: {invitation.channel.title}</Card.Title>
                        <Card.Text>
                          Invité par: {invitation.user.firstName} {invitation.user.lastName}
                        </Card.Text>
                        <div className="d-flex gap-2">
                          {/* Ajouter un bouton pour accepter l'invitation */}
                          <Button
                              variant="success"
                              size="sm"
                              onClick={() => handleAccept(invitation)}
                          >
                            Accepter
                          </Button>
                          {/* Ajouter un bouton pour refuser l'invitation */}
                          <Button
                              variant="danger"
                              size="sm"
                              onClick={() => handleReject(invitation)}
                          >
                            Refuser
                          </Button>
                        </div>
                      </Card.Body>
                    </Card>
                  </Col>
              ))}
            </Row>
        )}

        {/* Créer un canal */}
        <h2 className="mb-4">Créer un canal</h2>
        <CanalForm />

        {/* Modal d'édition de canal */}
        <EditChannelModal
            show={showEditModal}
            handleClose={() => {
              setShowEditModal(false);
              setSelectedChannel(null);
            }}
            channel={selectedChannel}
            onUpdate={loadUserChannels}
        />
      </Container>
  );
};

export default UserChannels;
