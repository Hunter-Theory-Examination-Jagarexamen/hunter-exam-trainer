import React from "react";
import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import "../../styles/legal.css";

interface LegalPageProps {
    title: string;
    children: React.ReactNode;
}

// Full-page frame for the Terms of Use and Privacy Policy (#212), so they can be
// read again after registration. The same texts open as a pop-up in the
// registration form (LegalDialog).
export const LegalPage: React.FC<LegalPageProps> = ({ title, children }) => {
    const { t } = useTranslation("legal");
    return (
        <main className="legal-page">
            <h1>{title}</h1>
            <p className="legal-temporary">{t("temporaryNote")}</p>
            {children}
            {/* Logged-out visitors are sent on to the login page by the protected route. */}
            <p className="legal-back">
                <Link to="/dashboard">{t("toStart")}</Link>
            </p>
        </main>
    );
};

export default LegalPage;
