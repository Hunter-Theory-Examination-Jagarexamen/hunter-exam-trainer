import React from "react";
import { useTranslation } from "react-i18next";

// Body text of the Terms of Use, shown inside LegalDialog (#212).
export const TermsContent: React.FC = () => {
    const { t } = useTranslation("legal");
    return (
        <>
            <p>{t("terms.intro")}</p>
            <ul>
                <li>{t("terms.forPractice")}</li>
                <li>{t("terms.mayChange")}</li>
                <li>{t("terms.personalAccount")}</li>
                <li>{t("terms.reportErrors")}</li>
            </ul>
        </>
    );
};

export default TermsContent;
