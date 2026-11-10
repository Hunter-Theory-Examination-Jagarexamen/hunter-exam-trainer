import { useState } from "react";
import { useTranslation } from "react-i18next";
import LegalDialog from "./LegalDialog";
import TermsContent from "./TermsContent";
import PrivacyContent from "./PrivacyContent";
import "../../styles/legal.css";

// Two small links, "Användarvillkor · Integritetspolicy", that open the texts
// in pop-ups (#212). Used at the bottom of the Profile page, so logged-in
// students can read them again without a new menu item.
const LegalLinks = () => {
    const { t } = useTranslation("legal");
    const [open, setOpen] = useState<"terms" | "privacy" | null>(null);

    return (
        <div className="legal-links">
            <button type="button" className="legal-link" onClick={() => setOpen("terms")}>
                {t("terms.title")}
            </button>
            <span aria-hidden="true"> · </span>
            <button type="button" className="legal-link" onClick={() => setOpen("privacy")}>
                {t("privacy.title")}
            </button>

            {open === "terms" && (
                <LegalDialog title={t("terms.title")} onClose={() => setOpen(null)}>
                    <TermsContent />
                </LegalDialog>
            )}
            {open === "privacy" && (
                <LegalDialog title={t("privacy.title")} onClose={() => setOpen(null)}>
                    <PrivacyContent />
                </LegalDialog>
            )}
        </div>
    );
};

export default LegalLinks;
