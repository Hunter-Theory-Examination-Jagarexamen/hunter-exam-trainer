import { useTranslation } from "react-i18next";
import LegalPage from "./LegalPage";
import TermsContent from "../../components/legal/TermsContent";

const TermsPage = () => {
    const { t } = useTranslation("legal");
    return (
        <LegalPage title={t("terms.title")}>
            <TermsContent />
        </LegalPage>
    );
};

export default TermsPage;
