import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Button, Alert, Spinner } from 'react-bootstrap';
import { userService, channelService } from '../services/api';
import { useAuth } from '../contexts/AuthContext';
import CanalForm from '../components/CanalForm';
import EditChannelModal from '../components/EditChannelModal';

const UserChannels = () => {
  const [channels, setChannels] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedChannel, setSelectedChannel] = useState(null);
  const [showEditModal, setShowEditModal] = useState(false);
  const { user } = useAuth();

  const loadUserChannels = async () => {
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
  };

  useEffect(() => {
    if (user) {
      loadUserChannels();
    }
  }, [user]);

  const handleEdit = (channel) => {
    setSelectedChannel(channel);
    setShowEditModal(true);
  };

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
      <h2 className="mb-4">Mes invitations</h2>
      <h2 className="mb-4">Créer un canal</h2>
      <CanalForm />
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