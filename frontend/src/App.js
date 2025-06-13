import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { Container, Spinner } from 'react-bootstrap';
import Navigation from './components/Navigation';
import ChatRoom from './pages/ChatRoom';
import UserChannels from './pages/UserChannels';
import Login from './pages/Login';
import Register from './pages/Register';
import { AuthProvider, useAuth } from './contexts/AuthContext';
import 'bootstrap/dist/css/bootstrap.min.css';

// Composant pour protéger les routes
const ProtectedRoute = ({ children }) => {
    const { user, loading } = useAuth();

    if (loading) {
        return (
            <div className="d-flex justify-content-center align-items-center" style={{ height: '100vh' }}>
                <Spinner animation="border" role="status">
                    <span className="visually-hidden">Chargement...</span>
                </Spinner>
            </div>
        );
    }

    if (!user) {
        return <Navigate to="/login" replace />;
    }

    return children;
};

function AppRoutes() {
    return (
        <>
            <Navigation />
            <div className="main-content">
                
                    <Routes>
                        <Route path="/login" element={<Login />} />
                        <Route path="/register" element={<Register />} />
                        <Route
                            path="/channels"
                            element={
                                <ProtectedRoute>
                                    <UserChannels />
                                </ProtectedRoute>
                            }
                        />
                        <Route path="/" element={<Navigate to="/channels" replace />} />
                        <Route
                            path="/chat/:channelId"
                            element={
                                <ProtectedRoute>
                                    <ChatRoom />
                                </ProtectedRoute>
                            }
                        />
                    </Routes>
                
            </div>
        </>
    );
}

function App() {
    return (
        <Router>
            <AuthProvider>
                <AppRoutes />
            </AuthProvider>
        </Router>
    );
}

export default App; 