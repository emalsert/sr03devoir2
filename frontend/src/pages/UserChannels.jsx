import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Button, Alert, Spinner, Toast, ToastContainer, Modal } from 'react-bootstrap';
import { userService, channelService, invitationService } from '../services/api'; // Assurez-vous que invitationService est bien importé
import { useAuth } from '../contexts/AuthContext';
import CanalForm from '../components/CanalForm';
import EditChannelModal from '../components/EditChannelModal';
import { useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import LottieLoader from '../components/LottieLoader';

const UserChannels = () => {
  const [allUsers, setAllUsers] = useState([]);
  const [selectedUserId, setSelectedUserId] = useState('');
  const [selectedChannelId, setSelectedChannelId] = useState('');
  const [channels, setChannels] = useState([]);
  const [channelsOwner, setChannelsOwner] = useState([]);
  const [invitations, setInvitations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedChannel, setSelectedChannel] = useState(null);
  const [showEditModal, setShowEditModal] = useState(false);
  const { user } = useAuth();
  const navigate = useNavigate();
  const [createChannelAlert, setCreateChannelAlert] = useState(null);
  const [showToast, setShowToast] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [channelToDelete, setChannelToDelete] = useState(null);
  const [invitationAlert, setInvitationAlert] = useState(null);

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
        const users = await userService.getAllUsers(); 
        setAllUsers(users.filter(u => u.userId !== user.userId)); // Exclut soi-même
      } catch (err) {
        console.error('Erreur lors du chargement des utilisateurs', err);
      }
    };

    if (user) {
      fetchUsers();
    }
  }, [user, loadUserInvitations, loadUserChannels]);

  // Gérer l'édition d'un canal
  const handleEdit = (channel) => {
    setSelectedChannel(channel);
    setShowEditModal(true);
  };

  // Gérer la suppression d'un canal
  const handleDelete = async () => {
    if (!channelToDelete) return;
      try {
      await channelService.deleteChannel(channelToDelete.channelId);
      setShowDeleteModal(false);
      setChannelToDelete(null);
      } catch (err) {
        setError('Erreur lors de la suppression du canal');
        console.error('Error deleting channel:', err);
      setShowDeleteModal(false);
      setChannelToDelete(null);
    }
  };

  // Gérer l'acceptation d'une invitation
  const handleAccept = async (invitation) => {
    try {
      await invitationService.acceptInvitation(invitation.invitationId);
      await loadUserInvitations(); // Recharger les invitations
    } catch (err) {
      setError('Erreur lors de l\'acceptation de l\'invitation');
      console.error('Error accepting invitation:', err);
    }
  };

  // Gérer le rejet d'une invitation
  const handleReject = async (invitation) => {
    try {
      await invitationService.declineInvitation(invitation.invitationId);
      await loadUserInvitations(); // Recharger les invitations
    } catch (err) {
      setError('Erreur lors du rejet de l\'invitation');
      console.error('Error rejecting invitation:', err);
    }
  };

  const handleSendInvitation = async () => {
    if (!selectedUserId || !selectedChannelId) {
      setInvitationAlert({ success: false, message: "Veuillez sélectionner un utilisateur et un canal." });
      return;
    }
    try {
      await invitationService.sendInvitation(Number(selectedUserId), Number(selectedChannelId), user.userId);
      setInvitationAlert({ success: true, message: "Invitation envoyée avec succès !" });
    } catch (error) {
      setInvitationAlert({ success: false, message: error.response.data });
    }
  };

  // Ajouter la fonction handleJoin
  const handleJoin = (channelId) => {
    navigate(`/chat/${channelId}`);
  };

  const handleChannelCreated = (success, message) => {
    setCreateChannelAlert({ success, message });
    setShowToast(true);
    if (success) {
      setTimeout(() => {
        setCreateChannelAlert(null);
        setShowToast(false);
      }, 2000);
    } else {
      setTimeout(() => setShowToast(false), 2000);
    }
  };

  if (loading) {
    return (
        <div className="d-flex justify-content-center align-items-center" style={{ height: '50vh' }}>
          <LottieLoader size={250} />
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
      <Container className="p-4">
        <Container className="ios-container" id="mes-canaux">
        <h2 className="mb-4">Mes Canaux</h2>
        {channels.length === 0 ? (
            <Alert variant="info">Aucun canal trouvé</Alert>
        ) : (
            <Row>
              {channels.map((channel) => {
                const now = new Date();
                const channelStart = new Date(channel.date);
                const channelEnd = new Date(channelStart.getTime() + channel.durationMinutes * 60000);
                const canJoin = now >= channelStart && now <= channelEnd;

                return (
                  <Col key={channel.channelId} md={6} lg={4} className="mb-4">
                    <Card className="ios-card">
                      <Card.Body>
                        <Card.Title>{channel.title}</Card.Title>
                        <Card.Text className="channel-description-truncate">{channel.description}</Card.Text>
                        <div className="d-flex justify-content-between text-muted">
                          <small>Date: {new Date(channel.date).toLocaleString()}</small>
                          <small>Durée: {channel.durationMinutes} minutes</small>
                        </div>
                        <div className="d-flex gap-2 mt-3">
                          <Button
                              variant="success"
                              size="sm"
                              onClick={() => handleJoin(channel.channelId)}
                              disabled={!canJoin}
                              style={!canJoin ? { opacity: 0.5, pointerEvents: 'none', cursor: 'not-allowed' } : {}}
                          >
                            Rejoindre le salon
                          </Button>
                          {channel.owner?.userId === user.userId && (
                            <>
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
                                onClick={() => {
                                  setChannelToDelete(channel);
                                  setShowDeleteModal(true);
                                }}
                          >
                            Supprimer
                          </Button>
                            </>
                          )}
                        </div>
                      </Card.Body>
                    </Card>
                  </Col>
                );
              })}
            </Row>
        )}
        </Container>
        {/* Mes Invitations Section */}
        <Container className="ios-container" id="mes-invitations">
        <h2 className="mb-4">Mes Invitations</h2>
        {invitations.length === 0 ? (
            <Alert variant="info">Aucune invitation reçue</Alert>
        ) : (
            <Row>
              {invitations.map((invitation) => (
                  <Col key={invitation.invitationId} md={6} lg={4} className="mb-4">
                    <Card className="ios-card">
                      <Card.Body>
                        <Card.Title>Channel ID: {invitation.channel.title}</Card.Title>
                        <Card.Text>
                          Invité par: {invitation.channel.owner.email}
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
        </Container>

        <Container className="ios-container" id="invite-user">
        <h2 className="mb-4">Inviter un utilisateur</h2>
        {invitationAlert && (
          <Alert
            variant={invitationAlert.success ? "success" : "danger"}
            onClose={() => setInvitationAlert(null)}
            dismissible
            className="mb-3"
          >
            {invitationAlert.message}
          </Alert>
        )}
        <Row className="mb-4">
          <Col md={5}>
            <select
                className="form-select"
                value={selectedChannelId}
                onChange={(e) => setSelectedChannelId(e.target.value)}
            >
              <option value="">Sélectionnez un canal</option>
              {channels.map((ch) => ch.owner.userId === user.userId && (
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
        </Container>

        {/* Créer un canal */}
        <Container className="ios-container" id="create-channel">
        <h2 className="mb-4">Créer un canal</h2>
          {createChannelAlert && (
            <Alert
              variant={createChannelAlert.success ? "success" : "danger"}
              onClose={() => setCreateChannelAlert(null)}
              dismissible
              className="mb-3"
            >
              {createChannelAlert.message}
            </Alert>
          )}
          <CanalForm onChannelCreated={handleChannelCreated} />
        </Container>

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

        {/* Modal de suppression de canal */}
        <Modal show={showDeleteModal} onHide={() => setShowDeleteModal(false)}>
          <Modal.Header closeButton>
            <Modal.Title>Confirmer la suppression</Modal.Title>
          </Modal.Header>
          <Modal.Body>
            Êtes-vous sûr de vouloir supprimer le canal <strong>{channelToDelete?.title}</strong> ? Cette action est irréversible.
          </Modal.Body>
          <Modal.Footer>
            <Button variant="secondary" onClick={() => setShowDeleteModal(false)}>
              Annuler
            </Button>
            <Button variant="danger" onClick={handleDelete}>
              Supprimer
            </Button>
          </Modal.Footer>
        </Modal>
      </Container>
  );
};

export default UserChannels;
