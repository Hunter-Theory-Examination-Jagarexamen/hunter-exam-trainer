import apiClient from "../../api/apiClient";
import "../../styles/auth.css";
import {useState} from "react";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { Eye, EyeOff } from "lucide-react";
import logo from "../../assets/images/logo.svg";

const RegisterForm = () => {

    const { t } = useTranslation();
    const navigate = useNavigate();

    const [showPassword, setShowPassword] = useState(false);
    const [showConfirmPassword, setShowConfirmPassword] = useState(false);

    const [fullName, setFullName] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [acceptedTerms, setAcceptedTerms] = useState(false);

    const [error, setError] = useState("");
    const [isLoading, setIsLoading] = useState(false);

    // Handles form submission for new user registration
    const handleRegister = async (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
    // Validates that all form fields are populated
        if (!fullName || !email || !password || !confirmPassword) {
            setError(t("errors.MISSING_FIELDS"));
            return;
        }
    // Validates that password matches confirm password
        if (password !== confirmPassword) {
            setError(t("errors.PASSWORD_MISMATCH"));
            return;
        }
    // Validates minimum password length requirement of 8 characters
        if(password.length < 8) {
            setError(t("errors.PASSWORD_TOO_SHORT"));
            return;
        }
    // Validates that the user accepted terms and conditions
        if (!acceptedTerms) {
            setError(t("errors.TERMS_REQUIRED"));
            return;
        }
    // Sets loading state to true during API request
        setIsLoading(true);

        try {
            await apiClient("/api/auth/register", {
                method: "POST",
                body: JSON.stringify({
                    fullName,
                    email,
                    password,
                }),
            });
    // Redirects user to login page after successful registration
            navigate("/login", { state: { accountCreated: true } });
        } catch (err: unknown) {
            const errorCode = (err as { code?: string })?.code || "UNKNOWN_ERROR";
            setError(
                t(`errors.${errorCode}`,
                    err instanceof Error && err.message
                        ? err.message
                        : t("auth.registerFailed")
                )
            );
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="auth-form-container register-form-container">

            <div className="auth-form-header">
                <div className="auth-mobile-logo">
                    <img src={logo} alt="Hunter Exam Trainer" />
                </div>

                <h2>{t("auth.registerTitle")}</h2>

                <p className="">
                    {t("auth.hasAccount")}{" "}
                    <button
                        type="button"
                        onClick={() => navigate("/login")}
                    >
                        {t("auth.loginLink")}
                    </button>
                </p>
            </div>

            <form onSubmit={handleRegister}>
                <div className="register-fields">
                    {/* Full Name */}
                    <div className="form-field">
                        <label htmlFor="fullName">
                            {t("auth.fullNameLabel")}
                        </label>
                        <div className="input-wrapper">
                            <input
                                id="fullName"
                                type="text"
                                placeholder={t("auth.fullNamePlaceHolder")}
                                value={fullName}
                                onChange={(e) => setFullName(e.target.value)}
                            />
                        </div>
                    </div>

                    {/* Email */}
                    <div className="form-field">
                        <label htmlFor="email">
                            {t("auth.emailLabel")}
                        </label>
                        <div className="input-wrapper">
                            <input
                                id="email"
                                type="email"
                                placeholder={t("auth.emailPlaceholder")}
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                            />
                        </div>
                    </div>

                    {/* Password */}
                    <div className="form-field">
                        <label htmlFor="password">
                            {t("auth.passwordLabel")}
                        </label>
                        <div className="input-wrapper">
                            <input
                                id="password"
                                type={showPassword ? "text" : "password"}
                                placeholder={t("auth.createPasswordPlaceholder")}
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

                    {/* Confirm Password */}
                    <div className="form-field">
                        <label htmlFor="confirmPassword">
                            {t("auth.confirmPasswordLabel")}
                        </label>
                        <div className="input-wrapper">
                            <input
                                id="confirmPassword"
                                type={showConfirmPassword ? "text" : "password"}
                                placeholder={t("auth.confirmPasswordPlaceholder")}
                                value={confirmPassword}
                                onChange={(e) => setConfirmPassword(e.target.value)}
                            />
                            <button
                                type="button"
                                className="password-toggle"
                                onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                                aria-label={showConfirmPassword ? t("auth.hidePassword") : t("auth.showPassword")}
                            >
                                {showConfirmPassword ? <EyeOff size={18} /> : <Eye size={18} /> }
                            </button>
                        </div>
                    </div>
                </div>

                {/* Terms */}
                <div className="terms-field">
                    <label>
                        <input
                            type="checkbox"
                            name="terms"
                            checked={acceptedTerms}
                            onChange={(e) => setAcceptedTerms(e.target.checked)}
                        />
                        <span>
                            {t("auth.termsAgreement")}{" "}
                            <a href="/terms">{t("auth.termsOfUse")}</a>{" "}
                            {t("auth.and")}{" "}
                            <a href="/privacy">{t("auth.privacyPolicy")}</a>
                        </span>
                    </label>
                </div>

                {error && (
                    <p role="alert" style={{ color: "#e53e3e", marginBottom: "1rem", fontSize: "0.875rem" }}>
                        {error}
                    </p>
                )}

                {/* Submit */}
                <button
                    type="submit"
                    className="auth-primary-button"
                    disabled={isLoading}
                >
                    {isLoading ? t("auth.creatingAccount") : t("auth.registerButton")}
                </button>
            </form>
        </div>
    );
};

RegisterForm.displayName = "RegisterForm";

export default RegisterForm;
