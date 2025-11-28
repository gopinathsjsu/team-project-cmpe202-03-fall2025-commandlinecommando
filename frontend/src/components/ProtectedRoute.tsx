import React from 'react';
import { useAuth } from '../context/AuthContext';

type UserRole = 'BUYER' | 'SELLER' | 'ADMIN';

interface ProtectedRouteProps {
  children: React.ReactNode;
  requiredRole?: UserRole;           // Single role required
  requiredRoles?: UserRole[];        // Any of these roles required
  requireAllRoles?: UserRole[];      // All of these roles required
  fallback?: React.ReactNode;
}

export function ProtectedRoute({ 
  children, 
  requiredRole, 
  requiredRoles,
  requireAllRoles,
  fallback 
}: ProtectedRouteProps) {
  const { isAuthenticated, isLoading, user, hasRole, hasAnyRole } = useAuth();

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center">
          <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
          <p className="mt-4 text-gray-600">Loading...</p>
        </div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return fallback || (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center">
          <h2 className="text-2xl font-bold text-gray-900 mb-2">Access Denied</h2>
          <p className="text-gray-600">Please log in to continue</p>
        </div>
      </div>
    );
  }

  // Check single required role
  if (requiredRole && !hasRole(requiredRole)) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center">
          <h2 className="text-2xl font-bold text-gray-900 mb-2">Access Denied</h2>
          <p className="text-gray-600">You don't have permission to view this page</p>
          <p className="text-sm text-gray-500 mt-2">Required role: {requiredRole}</p>
        </div>
      </div>
    );
  }

  // Check if user has any of the required roles
  if (requiredRoles && requiredRoles.length > 0 && !hasAnyRole(requiredRoles)) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center">
          <h2 className="text-2xl font-bold text-gray-900 mb-2">Access Denied</h2>
          <p className="text-gray-600">You don't have permission to view this page</p>
          <p className="text-sm text-gray-500 mt-2">Required roles: {requiredRoles.join(' or ')}</p>
        </div>
      </div>
    );
  }

  // Check if user has all of the required roles
  if (requireAllRoles && requireAllRoles.length > 0) {
    const hasAllRoles = requireAllRoles.every(role => hasRole(role));
    if (!hasAllRoles) {
      return (
        <div className="min-h-screen flex items-center justify-center bg-gray-50">
          <div className="text-center">
            <h2 className="text-2xl font-bold text-gray-900 mb-2">Access Denied</h2>
            <p className="text-gray-600">You don't have permission to view this page</p>
            <p className="text-sm text-gray-500 mt-2">Required roles: {requireAllRoles.join(' and ')}</p>
          </div>
        </div>
      );
    }
  }

  return <>{children}</>;
}
