import {useEffect, useState} from "react";
import {useNavigate} from "react-router-dom";
import {useTranslation} from "react-i18next";
import {Lock} from "lucide-react";
import logo from "../../assets/images/logo.svg";
import "../../styles/auth.css";
import {resetPassword} from "../../services/authService";

const ResetPassword = () => {
    const {t} = useTranslation();
    const navigate = useNavigate();
    const [token] = useState(() => new URLSearchParams(window.location.hash.slice(1)).get("token") ?? "");
    const [password, setPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [error, setError] = useState("");
    const [message, setMessage] = useState("");
    const [isLoading, setIsLoading] = useState(false);
    const validToken = /^[A-Za-z0-9_-]{43}$/.test(token);

    useEffect(() => {
        // Keep the bearer reset token out of the address bar and browser history.
        window.history.replaceState(window.history.state, "", window.location.pathname);
    }, []);

    const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        setError("");
        if (password !== confirmPassword) {
            setError(t("resetPassword.passwordMismatch"));
            return;
        }
        if (password.length < 8 || new TextEncoder().encode(password).length > 72) {
            setError(t("resetPassword.passwordLengthError"));
            return;
        }
        setIsLoading(true);
        try {
            const response = await resetPassword(token, password);
            setMessage(response.message);
            setPassword("");
            setConfirmPassword("");
        } catch (err) {
            setError(err instanceof Error ? err.message : t("resetPassword.defaultError"));
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="auth-page login">
            <div className="auth-brand-panel">
                <div className="auth-brand-content">
                    <div className="auth-logo"><img src={logo} alt="Hunter Exam Trainer" /></div>
                    <h1>{t("resetPassword.brandTitle")}</h1>
                    <p>{t("resetPassword.brandSubtitle")}</p>
                </div>
            </div>
            <div className="auth-form-container">
                <div className="auth-form-header">
                    <div className="auth-mobile-logo"><img src={logo} alt="Hunter Exam Trainer" /></div>
                    <h2>{t("resetPassword.title")}</h2>
                    <p>{t("resetPassword.subtitle")}</p>
                </div>
                {!validToken && <p role="alert">{t("resetPassword.invalidToken")}</p>}
                {validToken && !message && (
                    <form onSubmit={handleSubmit}>
                        <div className="form-field">
                            <label htmlFor="password">{t("resetPassword.passwordLabel")}</label>
                            <div className="input-wrapper">
                                <Lock size={18} />
                                <input id="password" type="password" autoComplete="new-password"
                                       required minLength={8} value={password}
                                       onChange={(e) => setPassword(e.target.value)} />
                            </div>
                        </div>
                        <div className="form-field">
                            <label htmlFor="confirmPassword">{t("resetPassword.confirmPasswordLabel")}</label>
                            <div className="input-wrapper">
                                <Lock size={18} />
                                <input id="confirmPassword" type="password" autoComplete="new-password"
                                       required minLength={8} value={confirmPassword}
                                       onChange={(e) => setConfirmPassword(e.target.value)} />
                            </div>
                        </div>
                        {error && <p role="alert" style={{color: "#e53e3e"}}>{error}</p>}
                        <button type="submit" className="auth-primary-button" disabled={isLoading}>
                            {isLoading ? t("resetPassword.submittingButton") : t("resetPassword.submitButton")}
                        </button>
                    </form>
                )}
                {message && <div className="forgot-message" role="status">{message}</div>}
                {!message && <p className="auth-switch"><button type="button" onClick={() => navigate("/forgot-password")}>{t("resetPassword.requestNewLink")}</button></p>}
                <p className="auth-switch"><button type="button" onClick={() => navigate("/login")}>{t("resetPassword.backToLogin")}</button></p>
            </div>
        </div>
    );
};

export default ResetPassword;
