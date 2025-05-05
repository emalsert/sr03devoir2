import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Spinner, Alert } from 'react-bootstrap';
import { userService } from '../services/api';
import { useAuth } from '../contexts/AuthContext';
import CanalForm from '../components/CanalForm';
const UserChannels = () => {
  const [channels, setChannels] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const { user } = useAuth();

  useEffect(() => {
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

    if (user) {
      loadUserChannels();
    }
  }, [user]);

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
    <Container>
      <h2 className="mb-4">Mes Canaux</h2>
      {channels.length === 0 ? (
        <Alert variant="info">Aucun canal trouvé</Alert>
      ) : (
        <Row>
          {channels.map((channel) => (
            <Col key={channel.id} md={6} lg={4} className="mb-4">
              <Card>
                <Card.Body>
                  <Card.Title>{channel.title}</Card.Title>
                  <Card.Text>{channel.description}</Card.Text>
                  <div className="d-flex justify-content-between text-muted">
                    <small>Date: {new Date(channel.date).toLocaleDateString()}</small>
                    <small>Durée: {channel.duration} minutes</small>
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
    </Container>
  );
};

export default UserChannels; 