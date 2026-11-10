import React from 'react';
import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { getTokenStatus } from "../../utils/authToken.ts";

export const NotFound: React.FC = () => {
    const { t } = useTranslation();
    const isLoggedIn = getTokenStatus() === "valid";
    return (
        <div style={{ textAlign: 'center', padding: '3rem 1rem' }}>
            <h1>{t("notFound.title")}</h1>
            <p style={{ margin: '1rem 0 2rem' }}>
                {t("notFound.message")}
            </p>
            <Link
                to={isLoggedIn ? "/dashboard" : "/login"}
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
                {isLoggedIn ? t("notFound.dashboardButton") : t("notFound.loginButton")}
            </Link>
        </div>
    );
};

export default NotFound;
