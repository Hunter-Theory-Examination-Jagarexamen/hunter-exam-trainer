import {useEffect, useState} from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { useTranslation } from "react-i18next";
import {Eye, EyeOff, Lock, Mail} from "lucide-react";
import logo from "../../assets/images/logo.svg";
import "../../styles/auth.css";
import {
    checkGoogleLoginEnabled,
    loginUser
} from "../../services/authService";
import { API_BASE_URL } from "../../api/apiClient";

const LoginForm = () => {

    const { t } = useTranslation();
    const navigate = useNavigate();
    const location = useLocation();
    const [successMessage] = useState(
        (location.state as { accountCreated?: boolean })?.accountCreated
        ? t("auth.accountCreatedSuccess", "Account created successfully! You can now log in.")
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
            setError(t("errors.MISSING_CREDENTIALS", "Please enter your email and password"));
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

        } catch (err: any) {
            console.error("Login/API error:", err);
            const errorCode = err?.code || "INVALID_CREDENTIALS";
            setError(t(`errors.${errorCode}`,t("errors.UNKNOWN_ERROR", "An unexpected error occurred.")));
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
                <h2>{t("auth.loginTitle", "Welcome back!")}</h2>
                <p>{t("auth.loginSubtitle", "Log in to continue your practice.")}</p>
            </div>

            {successMessage && (
                <p role="status" style={{ color: "#38a169", marginBottom: "1rem", fontSize: "0.875rem", fontWeight: 500 }}>
                    {successMessage}
                </p>
            )}

            <form onSubmit={handleLogin}>
                <div className="form-field">
                    <label htmlFor="email">
                        {t("auth.emailLabel", "Email")}
                    </label>

                    <div className="input-wrapper">

                        <Mail size={18} />
                        <input
                            id="email"
                            type="email"
                            placeholder={t("auth.emailPlaceholder", "Enter your email")}
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                        />

                    </div>
                </div>

                <div className="form-field">
                    <label htmlFor="password">
                        {t("auth.passwordLabel", "Password")}
                    </label>

                    <div className="input-wrapper">

                        <Lock size={18} />
                        <input
                            id="password"
                            type={showPassword ? "text" : "password"}
                            placeholder={t("auth.passwordPlaceholder", "Enter your password")}
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                        />

                        <button
                            type="button"
                            className="password-toggle"
                            onClick={() => setShowPassword(!showPassword)}
                            aria-label={showPassword ? t("auth.hidePassword", "Hide Password") : t("auth.showPassword", "Show Password")}
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
                        {t("auth.forgotPassword", "Forgot Password?")}
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
                    {isLoading ? t("auth.loggingIn", "Logging in...") : t("auth.loginButton", "Log In")}
                </button>
            </form>

            {/* Google login is only shown when the backend has it configured (#61). */}
            {googleEnabled && (
                <>
                    <div className="auth-divider">
                        <span>{t("auth.or", "or")}</span>
                    </div>

                    <div className="auth-secondary-actions">
                        <button
                            type="button"
                            className="auth-secondary-button"
                            onClick={handleGoogleLogin}
                        >
                            {t("auth.googleLogin", "Continue with Google")}
                        </button>
                    </div>
                </>
            )}

            <p className="auth-switch">
                {t("auth.noAccount", "Don't have an account?")}{" "}
                <button
                    type="button"
                    onClick={() => navigate("/register")}
                >
                    {t("auth.signUp", "Sign Up")}
                </button>
            </p>
        </div>
    );
};

export default LoginForm;
