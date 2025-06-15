import React, { createContext, useContext, useState, useEffect } from 'react';
import { getCurrentUser, isAuthenticated } from '../services/api';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const checkAuth = async () => {
            try {
                const userData = await getCurrentUser();
                if (userData) {
                    setUser(userData);
                } else {
                    setUser(null);
                }
            } catch (err) {
                setError(err);
                setUser(null);
            } finally {
                setLoading(false);
            }
        };

        checkAuth();
    }, []);

    const value = {
        user,
        setUser,
        loading,
        error,
        isAuthenticated: !!user
    };

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth doit être utilisé dans un AuthProvider');
    }
    return context;
}; 