import React from "react";
import { useTranslation } from "react-i18next";

// Body text of the Privacy Policy, shown inside LegalDialog (#212).
export const PrivacyContent: React.FC = () => {
    const { t } = useTranslation("legal");
    return (
        <>
            <h3>{t("privacy.whatTitle")}</h3>
            <ul>
                <li>{t("privacy.whatNameEmail")}</li>
                <li>{t("privacy.whatPassword")}</li>
                <li>{t("privacy.whatAnswers")}</li>
                <li>{t("privacy.whatStats")}</li>
                <li>{t("privacy.whatFeedback")}</li>
            </ul>

            <h3>{t("privacy.whyTitle")}</h3>
            <ul>
                <li>{t("privacy.whyOwnProgress")}</li>
                <li>{t("privacy.whyCourseLeader")}</li>
            </ul>

            <h3>{t("privacy.whoTitle")}</h3>
            <ul>
                <li>{t("privacy.whoYou")}</li>
                <li>{t("privacy.whoAdmins")}</li>
                <li>{t("privacy.whoFeedback")}</li>
                <li>{t("privacy.whoNotShared")}</li>
            </ul>

            <h3>{t("privacy.whereTitle")}</h3>
            <p>{t("privacy.whereProviders")}</p>

            <h3>{t("privacy.howLongTitle")}</h3>
            <p>{t("privacy.howLongPeriod")}</p>

            <h3>{t("privacy.rightsTitle")}</h3>
            <p>{t("privacy.rightsText")}</p>

            <p>{t("privacy.responsible")}</p>
        </>
    );
};

export default PrivacyContent;
