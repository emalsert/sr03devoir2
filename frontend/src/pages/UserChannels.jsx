import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Button, Alert, Spinner } from 'react-bootstrap';
import { userService, channelService, invitationService } from '../services/api'; // Assurez-vous que invitationService est bien importé
import { useAuth } from '../contexts/AuthContext';
import CanalForm from '../components/CanalForm';
import EditChannelModal from '../components/EditChannelModal';
import { useCallback } from 'react';
import { useNavigate } from 'react-router-dom';

const UserChannels = () => {
  const [allUsers, setAllUsers] = useState([]);
  const [selectedUserId, setSelectedUserId] = useState('');
  const [selectedChannelId, setSelectedChannelId] = useState('');
  const [channels, setChannels] = useState([]);
  const [invitations, setInvitations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedChannel, setSelectedChannel] = useState(null);
  const [showEditModal, setShowEditModal] = useState(false);
  const { user } = useAuth();
  const navigate = useNavigate();

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

    const fetchUsers = async () => {
      try {
        const users = await userService.getAllUsers(); // Assure-toi que cette méthode existe
        setAllUsers(users.filter(u => u.userId !== user.userId)); // Exclut soi-même
      } catch (err) {
        console.error('Erreur lors du chargement des utilisateurs', err);
      }
    };

    if (user) {
      fetchUsers();
    }
  }, [user, loadUserChannels, loadUserInvitations]);

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

  const handleSendInvitation = async () => {
    if (!selectedUserId || !selectedChannelId) {
      alert("Veuillez sélectionner un utilisateur et un canal.");
      return;
    }

    try {
      await invitationService.sendInvitation(Number(selectedUserId), Number(selectedChannelId));
      alert("Invitation envoyée avec succès !");
    } catch (error) {
      console.error("Erreur lors de l'envoi de l'invitation :", error);
      alert("Erreur lors de l'envoi de l'invitation.");
    }
  };

  // Ajouter la fonction handleJoin
  const handleJoin = (channelId) => {
    navigate(`/chat/${channelId}`);
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
                        <div className="d-flex gap-2 mt-3">
                          <Button
                              variant="success"
                              size="sm"
                              onClick={() => handleJoin(channel.channelId)}
                          >
                            Rejoindre le salon
                          </Button>
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

        <h2 className="mb-4">Inviter un utilisateur</h2>
        <Row className="mb-4">
          <Col md={5}>
            <select
                className="form-select"
                value={selectedChannelId}
                onChange={(e) => setSelectedChannelId(e.target.value)}
            >
              <option value="">Sélectionnez un canal</option>
              {channels.map((ch) => (
                  <option key={ch.channelId} value={ch.channelId}>
                    {ch.title}
                  </option>
              ))}
            </select>
          </Col>
          <Col md={5}>
            <select
                className="form-select"
                value={selectedUserId}
                onChange={(e) => setSelectedUserId(e.target.value)}
            >
              <option value="">Sélectionnez un utilisateur</option>
              {allUsers.map((u) => (
                  <option key={u.userId} value={u.userId}>
                    {u.firstName} {u.lastName}
                  </option>
              ))}
            </select>
          </Col>
          <Col md={2}>
            <Button variant="primary" onClick={handleSendInvitation}>
              Envoyer
            </Button>
          </Col>
        </Row>

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
