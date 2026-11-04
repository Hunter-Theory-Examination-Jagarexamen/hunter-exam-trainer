import {useEffect, useState} from "react";
import { useNavigate, useLocation } from "react-router-dom";
import {Eye, EyeOff, Lock, Mail} from "lucide-react";
import logo from "../../assets/images/logo.svg";
import "../../styles/auth.css";
import {
    checkGoogleLoginEnabled,
    loginUser
} from "../../services/authService";
import { API_BASE_URL } from "../../api/apiClient";
import { errorStatus, isServerUnreachable } from "../../services/apiErrors";
import { useTranslation } from "react-i18next";

const LoginForm = () => {

    const { t } = useTranslation();
    const navigate = useNavigate();
    const location = useLocation();
    const [successMessage] = useState(
        (location.state as { accountCreated?: boolean })?.accountCreated
        ? t("auth.accountCreatedSuccess", "Konto skapat! Du kan nu logga in.")
            : ""
    );

    const [showPassword, setShowPassword] = useState(false);

    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [isLoading, setIsLoading] = useState(false);
    const [googleEnabled, setGoogleEnabled] = useState(false);
    const [error, setError] = useState("");

    useEffect(() => {
        checkGoogleLoginEnabled().then(setGoogleEnabled);
    }, []);

    const handleLogin = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(""); // clear the message from the previous attempt

        if(!email || !password) {
            setError(t("auth.enterEmailAndPassword", "Vänligen ange både e-postadress och lösenord"));
            return;
        }

        try {
            setIsLoading(true);

            const response = await loginUser({
                email,
                password,
            });

            localStorage.setItem("token", response.token);

            navigate("/dashboard");

        } catch (error) {
            console.error("Login/API error:", error);
            if (isServerUnreachable(error)) {
                setError(t("serverStatus.error", "Det går inte att nå servern. Kontrollera din internetanslutning eller försök igen om en stund."));
            } else if ((errorStatus(error) ?? 0) >= 500) {
                setError(t("errors.UNKNOWN_ERROR", "Ett oväntat fel uppstod på servern. Försök igen senare."));
            } else {
                setError(t("errors.INVALID_CREDENTIALS", "Fel e-postadress eller lösenord."));
            }
        } finally {
            setIsLoading(false);
        }

    };

    const handleGoogleLogin = () => {
        window.location.href = `${API_BASE_URL}/oauth2/authorization/google`;
    };

    return (
        <div className="auth-form-container">

            <div className="auth-form-header">
                <div className="auth-mobile-logo">
                    <img src={logo} alt="Hunter Exam Trainer" />
                </div>
                <h2>{t("auth.loginTitle", "Välkommen tillbaka!")}</h2>
                <p>{t("auth.loginSubtitle", "Logga in för att fortsätta öva.")}</p>
            </div>

            {successMessage && (
                <p role="status" style={{ color: "#38a169", marginBottom: "1rem", fontSize: "0.875rem", fontWeight: 500 }}>
                    {successMessage}
                </p>
            )}

            <form onSubmit={handleLogin}>
                <div className="form-field">
                    <label htmlFor="email">{t("auth.emailLabel", "E-post")}</label>

                    <div className="input-wrapper">

                        <Mail size={18} />
                        <input
                            id="email"
                            type="email"
                            placeholder={t("auth.emailPlaceholder", "Ange din e-postadress")}
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                        />

                    </div>
                </div>

                <div className="form-field">
                    <label htmlFor="password">{t("auth.passwordLabel", "Lösenord")}</label>

                    <div className="input-wrapper">

                        <Lock size={18} />
                        <input
                            id="password"
                            type={showPassword ? "text" : "password"}
                            placeholder={t("auth.passwordPlaceholder", "Ange ditt lösenord")}
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                        />

                        <button
                            type="button"
                            className="password-toggle"
                            onClick={() => setShowPassword(!showPassword)}
                            aria-label={showPassword ? t("auth.hidePassword", "Dölj lösenord") : t("auth.showPassword", "Visa lösenord")}
                        >
                            {showPassword ? <EyeOff size={18} /> : <Eye size={18} /> }
                        </button>

                    </div>

                </div>

                <div className="forgot-password">
                    <button
                        type="button"
                        onClick={() => navigate("/forgot-password")}
                    >
                        {t("auth.forgotPassword", "Glömt lösenord?")}
                    </button>
                </div>

                {error && (
                    <p role="alert" style={{ color: "#e53e3e", marginBottom: "1rem", fontSize: "0.875rem" }}>
                        {error}
                    </p>
                )}

                <button
                    type="submit"
                    className="auth-primary-button"
                    disabled={isLoading}
                >
                    {isLoading ? t("common.loading", "Loggar in...") : t("auth.loginButton", "Logga in")}
                </button>
            </form>

            {/* Google login is only shown when the backend has it configured (#61). */}
            {googleEnabled && (
                <>
                    <div className="auth-divider">
                        <span>{t("common.or", "eller")}</span>
                    </div>

                    <div className="auth-secondary-actions">
                        <button
                            type="button"
                            className="auth-secondary-button"
                            onClick={handleGoogleLogin}
                        >
                            {t("auth.continueWithGoogle", "Fortsätt med Google")}
                        </button>
                    </div>
                </>
            )}

            <p className="auth-switch">
                {t("auth.noAccount", "Har du inget konto?")}{" "}
                <button
                    type="button"
                    onClick={() => navigate("/register")}
                >
                    {t("auth.registerButton", "Registrera dig")}
                </button>
            </p>
        </div>
    );
};

export default LoginForm;
