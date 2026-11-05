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
        ? t("auth.accountCreatedSuccess")
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
            setError(t("auth.enterEmailAndPassword"));
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
                setError(t("serverStatus.error"));
            } else if ((errorStatus(error) ?? 0) >= 500) {
                setError(t("errors.UNKNOWN_ERROR"));
            } else {
                setError(t("errors.INVALID_CREDENTIALS"));
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
                <h2>{t("auth.welcomeBack")}</h2>
                <p>{t("auth.loginSubtitle")}</p>
            </div>

            {successMessage && (
                <p role="status" style={{ color: "#38a169", marginBottom: "1rem", fontSize: "0.875rem", fontWeight: 500 }}>
                    {successMessage}
                </p>
            )}

            <form onSubmit={handleLogin}>
                <div className="form-field">
                    <label htmlFor="email">{t("auth.emailLabel")}</label>

                    <div className="input-wrapper">

                        <Mail size={18} />
                        <input
                            id="email"
                            type="email"
                            placeholder={t("auth.emailPlaceholder")}
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                        />

                    </div>
                </div>

                <div className="form-field">
                    <label htmlFor="password">{t("auth.passwordLabel")}</label>

                    <div className="input-wrapper">

                        <Lock size={18} />
                        <input
                            id="password"
                            type={showPassword ? "text" : "password"}
                            placeholder={t("auth.passwordPlaceholder")}
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                        />

                        <button
                            type="button"
                            className="password-toggle"
                            onClick={() => setShowPassword(!showPassword)}
                            aria-label={showPassword ? t("auth.hidePassword") : t("auth.showPassword")}
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
                        {t("auth.forgotPassword")}
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
                    {isLoading ? t("auth.loggingIn") : t("auth.loginButton")}
                </button>
            </form>

            {/* Google login is only shown when the backend has it configured (#61). */}
            {googleEnabled && (
                <>
                    <div className="auth-divider">
                        <span>{t("common.or")}</span>
                    </div>

                    <div className="auth-secondary-actions">
                        <button
                            type="button"
                            className="auth-secondary-button"
                            onClick={handleGoogleLogin}
                        >
                            {t("auth.continueWithGoogle")}
                        </button>
                    </div>
                </>
            )}

            <p className="auth-switch">
                {t("auth.noAccount")}{" "}
                <button
                    type="button"
                    onClick={() => navigate("/register")}
                >
                    {t("auth.goToRegister")}
                </button>
            </p>
        </div>
    );
};

export default LoginForm;
