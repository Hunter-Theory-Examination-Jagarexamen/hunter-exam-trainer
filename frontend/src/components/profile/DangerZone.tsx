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

    // Prefer the backend's code if present (#243); fall back to the English
    // message so any other failure still shows something useful.
    const messageOf = (err: unknown): string => {
        if (err instanceof ApiError) {
            try {
                const body = JSON.parse(err.message);
                if (body?.code === "WRONG_PASSWORD") return t("errors.WRONG_PASSWORD");
                if (body?.code === "LAST_ADMIN") return t("errors.LAST_ADMIN");
                if (typeof body?.message === "string") return body.message;
            } catch { /* not JSON */ }
            return err.message;
        }
        return t("errors.UNKNOWN_ERROR");
    };

    const onSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);
        setBusy(true);
        try {
            await deleteMyAccount(password);
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
                    className="btn btn-danger"
                    onClick={() => setOpen(true)}
                >
                    {t("profile.deleteAccountButton")}
                </button>
            ) : (
                <form onSubmit={onSubmit} className="change-password-form">
                    <p className="danger-warning">
                        {t("profile.deleteAccountWarning")}
                    </p>
                    <div className="form-group">
                        <label htmlFor="delete-password">
                            {t("profile.currentPasswordLabel")}
                        </label>
                        <input
                            id="delete-password"
                            type="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            autoComplete="current-password"
                            required
                        />
                    </div>
                    {error && <p className="password-error">{error}</p>}
                    <div className="change-password-actions">
                        <button type="button" className="btn btn-secondary" onClick={cancel} disabled={busy}>
                            {t("common.cancel")}
                        </button>
                        <button
                            type="submit"
                            className="btn btn-danger"
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
