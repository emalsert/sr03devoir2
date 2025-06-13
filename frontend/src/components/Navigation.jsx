import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { Nav, Container, Button } from 'react-bootstrap';
import { useAuth } from '../contexts/AuthContext';
import Logout from './Logout';
import EditUserModal from './EditUserModal';

const Navigation = () => {
    const { user, loading } = useAuth();
    const [showEditUserModal, setShowEditUserModal] = useState(false);

    const handleUserUpdate = () => {
        setShowEditUserModal(false);
    };

    if (loading) {
        return null;
    }

    return (
        <div className="sidebar">
            <div className="sidebar-header">
                <h3 className="sidebar-brand">PowerChat 📱</h3>
            </div>
            <Nav className="flex-column sidebar-nav">
                {user ? (
                    <>
                        <Nav.Link as="a" href="/channels#mes-canaux" className="sidebar-link">
                            <i className="fas fa-comments me-2"></i>
                            Canaux
                        </Nav.Link>
                        <Nav.Link as="a" href="/channels#create-channel" className="sidebar-link">
                            <i className="fas fa-plus me-2"></i>
                            Canal
                        </Nav.Link>
                        <Nav.Link as="a" href="/channels#mes-invitations" className="sidebar-link">
                            <i className="fas fa-envelope-open-text me-2"></i>
                            Invitations
                        </Nav.Link>
                    </>
                ) : (
                    <>
                        <Nav.Link as={Link} to="/login" className="sidebar-link">
                            <i className="fas fa-sign-in-alt me-2"></i>
                            Connexion
                        </Nav.Link>
                        <Nav.Link as={Link} to="/register" className="sidebar-link">
                            <i className="fas fa-user-plus me-2"></i>
                            Inscription
                        </Nav.Link>
                    </>
                )}
            </Nav>
            {user && (
                <div className="sidebar-footer">
                    <div className="user-info">
                        <i className="fas fa-user ms-2"></i>
                        {user.email}
                        <Button className="btn-primary ms-2 mt-1" onClick={() => setShowEditUserModal(true)}>Modifier</Button>
                        <EditUserModal show={showEditUserModal} handleClose={() => setShowEditUserModal(false)} user={user} onUpdate={handleUserUpdate} />
                    </div>
                    <Logout />
                </div>
            )}
        </div>
    );
};

export default Navigation; 