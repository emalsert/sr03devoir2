import React from 'react';
import { Button } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import { logout } from '../services/api';
import { useAuth } from '../contexts/AuthContext';

const Logout = () => {
  const navigate = useNavigate();
  const { setUser } = useAuth();

  const handleLogout = async () => {
    try {
      await logout();
      setUser(null);
      navigate('/login', { replace: true });
    } catch (error) {
      console.error('Erreur lors de la déconnexion:', error);
    }
  };

  return (
    <Button 
      variant="danger" 
      onClick={handleLogout}
      className="ms-2"
    >
      Déconnexion
    </Button>
  );
};

export default Logout; 