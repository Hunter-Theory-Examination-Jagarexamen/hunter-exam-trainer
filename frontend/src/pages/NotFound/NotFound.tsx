import React from 'react';
import { Link } from "react-router-dom";

export const NotFound: React.FC = () => {
    return (
        <div style={{ textAlign: 'center', padding: '3rem 1rem' }}>
            <h1>404 - Page Not Found</h1>
            <p style={{ margin: '1rem 0 2rem' }}>
                The page you are looking for does not exist or has been moved.
            </p>
            <Link
                to="/dashboard"
                style={{
                    display: 'inline-block',
                    padding: '0.75rem 1.5rem',
                    backgroundColor: '#2563eb',
                    color: '#ffffff',
                    borderRadius: '0.375rem',
                    textDecoration: 'none',
                    fontWeight: 600,
                }}
            >
                Go to Dashboard
            </Link>
        </div>
    );
};

export default NotFound;