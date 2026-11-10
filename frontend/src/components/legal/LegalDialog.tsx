import React, { useEffect } from "react";
import { useTranslation } from "react-i18next";
import "../../styles/legal.css";

interface LegalDialogProps {
    title: string;
    onClose: () => void;
    children: React.ReactNode;
}

// A pop-up (modal) for the Terms of Use and Privacy Policy (#212), so students
// read them without leaving the registration form or the installed app.
// Closes with the close button, the Escape key, or a click outside the card.
const LegalDialog = ({ title, onClose, children }: LegalDialogProps) => {
    const { t } = useTranslation("legal");

    // Escape closes the dialog. The listener is removed again when the dialog closes.
    useEffect(() => {
        const onKeyDown = (event: KeyboardEvent) => {
            if (event.key === "Escape") onClose();
        };
        window.addEventListener("keydown", onKeyDown);
        return () => window.removeEventListener("keydown", onKeyDown);
    }, [onClose]);

    return (
        // A click on the dark backdrop closes; clicks inside the card don't reach it.
        <div className="legal-backdrop" onClick={onClose}>
            <div
                className="legal-dialog"
                role="dialog"
                aria-modal="true"
                aria-labelledby="legal-title"
                onClick={(event) => event.stopPropagation()}
            >
                <h2 id="legal-title">{title}</h2>
                <p className="legal-temporary">{t("temporaryNote")}</p>
                {children}
                <div className="legal-actions">
                    <button type="button" className="check-answer-button" onClick={onClose}>
                        {t("close")}
                    </button>
                </div>
            </div>
        </div>
    );
};

export default LegalDialog;
