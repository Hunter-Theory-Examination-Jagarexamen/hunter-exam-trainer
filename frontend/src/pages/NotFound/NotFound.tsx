import React from 'react';
import { Link } from "react-router-dom";

export const NotFound: React.FC = () => {
    return (
        <div style={{ textAlign: 'center', padding: '3rem 1rem' }}>
            <h1>404 - Sidan hittades inte</h1>
            <p style={{ margin: '1rem 0 2rem' }}>
                Sidan du letar efter finns inte eller har flyttats.
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
                Gå till instrumentpanelen
            </Link>
        </div>
    );
};

export default NotFound;
