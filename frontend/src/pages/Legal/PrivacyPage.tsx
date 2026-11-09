import { useTranslation } from "react-i18next";
import LegalPage from "./LegalPage";
import PrivacyContent from "../../components/legal/PrivacyContent";

const PrivacyPage = () => {
    const { t } = useTranslation("legal");
    return (
        <LegalPage title={t("privacy.title")}>
            <PrivacyContent />
        </LegalPage>
    );
};

export default PrivacyPage;
