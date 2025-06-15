import React from 'react';
import { Container, Row, Col, Card } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import RegisterForm from '../components/auth/RegisterForm';

// Page d'inscription utilisateur
const Register = () => {
    return (
        <Container className="py-5">
            <Row className="justify-content-center">
                <Col md={6} lg={4}>
                    <Card>
                        <Card.Body>
                            <Card.Title className="text-center mb-4">Inscription</Card.Title>
                            <RegisterForm />
                            <div className="text-center mt-3">
                                <Link to="/login" className="text-decoration-none">
                                    Déjà un compte ? Se connecter
                                </Link>
                            </div>
                        </Card.Body>
                    </Card>
                </Col>
            </Row>
        </Container>
    );
};

export default Register; 