import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import AuthService from '../services/auth.service';

const ProtectedRoute = ({ children, requiredRole }) => {
    const location = useLocation();

    if (!AuthService.isTokenValid()) {
        return <Navigate to="/login" state={{ from: location }} replace />;
    }

    if (requiredRole && !AuthService.hasRole(requiredRole)) {
        // Redirect to home if accessing unauthorized role pages
        return <Navigate to="/" replace />;
    }

    return children;
};

export default ProtectedRoute;
