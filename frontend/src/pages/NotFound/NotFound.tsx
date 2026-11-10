import React from 'react';
import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { getTokenStatus } from "../../utils/authToken";
import "../../styles/notfound.css";

export const NotFound: React.FC = () => {
    const { t } = useTranslation();
    const isLoggedIn = getTokenStatus() === "valid";
    return (
        <div className="not-found">
            <h1>{t("notFound.title")}</h1>
            <p>
                {t("notFound.message")}
            </p>
            <Link
                to={isLoggedIn ? "/dashboard" : "/login"}
                className="not-found-button"
            >
                {isLoggedIn ? t("notFound.dashboardButton") : t("notFound.loginButton")}
            </Link>
        </div>
    );
};

export default NotFound;
