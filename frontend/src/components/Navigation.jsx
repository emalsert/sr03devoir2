import React from 'react';
import { Link } from 'react-router-dom';
import { Navbar, Nav, Container } from 'react-bootstrap';
import { useAuth } from '../contexts/AuthContext';
import Logout from './Logout';

const Navigation = () => {
    const { user, loading } = useAuth();

    if (loading) {
        return null;
    }

    return (
        <Navbar bg="dark" variant="dark" expand="lg">
            <Container>
                <Navbar.Brand as={Link} to="/">Chat App</Navbar.Brand>
                <Navbar.Toggle aria-controls="basic-navbar-nav" />
                <Navbar.Collapse id="basic-navbar-nav">
                    <Nav className="me-auto">
                        {user ? (
                            <>
                                <Nav.Link as={Link} to="/channels">Canaux</Nav.Link>
                            </>
                        ) : (
                            <>
                                <Nav.Link as={Link} to="/login">Connexion</Nav.Link>
                                <Nav.Link as={Link} to="/register">Inscription</Nav.Link>
                            </>
                        )}
                    </Nav>
                    {user && (
                        <Nav>
                            <Navbar.Text className="me-3">
                                Connecté en tant que: {user.email}
                            </Navbar.Text>
                            <Logout />
                        </Nav>
                    )}
                </Navbar.Collapse>
            </Container>
        </Navbar>
    );
};

export default Navigation; 