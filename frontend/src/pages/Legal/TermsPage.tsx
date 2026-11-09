import React from "react";
import { useTranslation } from "react-i18next";
import LegalPage from "./LegalPage";

export const TermsPage: React.FC = () => {
    const { t } = useTranslation("legal");
    return (
        <LegalPage title={t("terms.title")}>
            <p>{t("terms.intro")}</p>
            <ul>
                <li>{t("terms.forPractice")}</li>
                <li>{t("terms.mayChange")}</li>
                <li>{t("terms.personalAccount")}</li>
                <li>{t("terms.reportErrors")}</li>
            </ul>
        </LegalPage>
    );
};

export default TermsPage;
