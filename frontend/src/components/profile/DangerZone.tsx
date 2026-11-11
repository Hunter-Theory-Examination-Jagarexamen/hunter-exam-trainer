import { useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import { deleteMyAccount } from "../../api/userApi";
import { ApiError } from "../../api/apiClient";
import "../../styles/profile.css";

const DangerZone = () => {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const [open, setOpen] = useState(false);
    const [password, setPassword] = useState("");
    const [error, setError] = useState<string | null>(null);
    const [busy, setBusy] = useState(false);

    // Backend returns {"message": "..."} for handled errors.
    const messageOf = (err: unknown): string => {
        if (err instanceof ApiError) {
            try {
                const body = JSON.parse(err.message);
                return typeof body?.message === "string" ? body.message : err.message;
            } catch {
                return err.message;
            }
        }
        return t("errors.UNKNOWN_ERROR");
    };

    const onSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);
        setBusy(true);
        try {
            await deleteMyAccount(password);
            // Reuse the existing logout flow, but signal why we're logging out.
            navigate("/logout?deleted=1", { replace: true });
        } catch (err) {
            setError(messageOf(err));
        } finally {
            setBusy(false);
        }
    };

    const cancel = () => {
        setOpen(false);
        setPassword("");
        setError(null);
    };

    return (
        <section className="danger-zone">
            <h2>{t("profile.dangerZoneTitle")}</h2>
            <p>{t("profile.dangerZoneText")}</p>

            {!open ? (
                <button
                    type="button"
                    className="btn-danger"
                    onClick={() => setOpen(true)}
                >
                    {t("profile.deleteAccountButton")}
                </button>
            ) : (
                <form onSubmit={onSubmit} className="danger-zone-form">
                    <p className="danger-warning">
                        {t("profile.deleteAccountWarning")}
                    </p>
                    <label>
                        {t("profile.currentPasswordLabel")}
                        <input
                            type="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            autoComplete="current-password"
                            required
                        />
                    </label>
                    {error && <p className="form-error">{error}</p>}
                    <div className="form-actions">
                        <button type="button" onClick={cancel} disabled={busy}>
                            {t("common.cancel")}
                        </button>
                        <button
                            type="submit"
                            className="btn-danger"
                            disabled={busy || !password}
                        >
                            {busy
                                ? t("profile.deletingAccount")
                                : t("profile.deleteAccountConfirm")}
                        </button>
                    </div>
                </form>
            )}
        </section>
    );
};

export default DangerZone;
