import React from 'react';
import { Container, Row, Col, Card } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import LoginForm from '../components/auth/LoginForm';

const Login = () => {
    return (
        <Container className="py-5">
            <Row className="justify-content-center">
                <Col md={6} lg={4}>
                    <Card>
                        <Card.Body>
                            <Card.Title className="text-center mb-4">Connexion</Card.Title>
                            <LoginForm />
                            <div className="text-center mt-3">
                                <Link to="/register" className="text-decoration-none">
                                    Pas encore de compte ? S'inscrire
                                </Link>
                            </div>
                        </Card.Body>
                    </Card>
                </Col>
            </Row>
        </Container>
    );
};

export default Login; 