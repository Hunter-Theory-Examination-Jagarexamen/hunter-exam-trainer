import React from "react";
import { useTranslation } from "react-i18next";
import LegalPage from "./LegalPage";

export const PrivacyPage: React.FC = () => {
    const { t } = useTranslation("legal");
    return (
        <LegalPage title={t("privacy.title")}>
            <h2>{t("privacy.whatTitle")}</h2>
            <ul>
                <li>{t("privacy.whatNameEmail")}</li>
                <li>{t("privacy.whatPassword")}</li>
                <li>{t("privacy.whatAnswers")}</li>
                <li>{t("privacy.whatStats")}</li>
                <li>{t("privacy.whatFeedback")}</li>
            </ul>

            <h2>{t("privacy.whyTitle")}</h2>
            <ul>
                <li>{t("privacy.whyOwnProgress")}</li>
                <li>{t("privacy.whyCourseLeader")}</li>
            </ul>

            <h2>{t("privacy.whoTitle")}</h2>
            <ul>
                <li>{t("privacy.whoYou")}</li>
                <li>{t("privacy.whoAdmins")}</li>
                <li>{t("privacy.whoFeedback")}</li>
                <li>{t("privacy.whoNotShared")}</li>
            </ul>

            <h2>{t("privacy.whereTitle")}</h2>
            <p>{t("privacy.whereProviders")}</p>

            <h2>{t("privacy.howLongTitle")}</h2>
            <p>{t("privacy.howLongPeriod")}</p>

            <h2>{t("privacy.rightsTitle")}</h2>
            <p>{t("privacy.rightsText")}</p>

            <p>{t("privacy.responsible")}</p>
        </LegalPage>
    );
};

export default PrivacyPage;
