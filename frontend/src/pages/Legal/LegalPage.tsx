import React from "react";
import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";

// Shared frame for the Terms of Use and Privacy Policy pages (#212):
// a title, a "temporary version" note, the page's own text, and a link back.
interface LegalPageProps {
    title: string;
    children: React.ReactNode;
}

export const LegalPage: React.FC<LegalPageProps> = ({ title, children }) => {
    const { t } = useTranslation("legal");
    return (
        <main style={{ maxWidth: "40rem", margin: "0 auto", padding: "2rem 1rem", lineHeight: 1.6 }}>
            <h1>{title}</h1>
            <p style={{ fontStyle: "italic" }}>{t("temporaryNote")}</p>
            {children}
            <p style={{ marginTop: "2rem" }}>
                <Link to="/login">{t("backToLogin")}</Link>
            </p>
        </main>
    );
};

export default LegalPage;
