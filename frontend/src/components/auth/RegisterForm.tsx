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
            setError(t("errors.MISSING_FIELDS", "Please fill in all the fields."));
            return;
        }
    // Validates that password matches confirm password
        if (password !== confirmPassword) {
            setError(t("errors.PASSWORD_MISMATCH", "Passwords do not match."));
            return;
        }
    // Validates minimum password length requirement of 8 characters
        if(password.length < 8) {
            setError(t("errors.PASSWORD_TOO_SHORT", "Password must be at least 8 characters long."));
            return;
        }
    // Validates that the user accepted terms and conditions
        if (!acceptedTerms) {
            setError(t("errors.TERMS_REQUIRED", "Please accept the Terms of Use and Privacy Policy."));
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
                        : t("errors.UNKNOWN_ERROR", "Registration failed. Please try again.")
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

                <h2>{t("auth.registerTitle", "Create Account")}</h2>

                <p className="">
                    {t("auth.hasAccount", "Already have an account?")}{" "}
                    <button
                        type="button"
                        onClick={() => navigate("/login")}
                    >
                        {t("auth.loginLink", "Log In")}
                    </button>
                </p>
            </div>

            <form onSubmit={handleRegister}>
                <div className="register-fields">
                    {/* Full Name */}
                    <div className="form-field">
                        <label htmlFor="fullName">
                            {t("auth.fullNameLabel", "Full Name")}
                        </label>
                        <div className="input-wrapper">
                            <input
                                id="fullName"
                                type="text"
                                placeholder={t("auth.fullNamePlaceHolder", "Enter your full name")}
                                value={fullName}
                                onChange={(e) => setFullName(e.target.value)}
                            />
                        </div>
                    </div>

                    {/* Email */}
                    <div className="form-field">
                        <label htmlFor="email">
                            {t("auth.emailLabel", "Email")}
                        </label>
                        <div className="input-wrapper">
                            <input
                                id="email"
                                type="email"
                                placeholder={t("auth.emailPlaceholder", "Enter your email")}
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                            />
                        </div>
                    </div>

                    {/* Password */}
                    <div className="form-field">
                        <label htmlFor="password">
                            {t("auth.passwordLabel", "Password")}
                        </label>
                        <div className="input-wrapper">
                            <input
                                id="password"
                                type={showPassword ? "text" : "password"}
                                placeholder={t("auth.passwordPlaceholder", "Create a password")}
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

                    {/* Confirm Password */}
                    <div className="form-field">
                        <label htmlFor="confirmPassword">
                            {t("auth.confirmPasswordLabel", "Confirm Password")}
                        </label>
                        <div className="input-wrapper">
                            <input
                                id="confirmPassword"
                                type={showConfirmPassword ? "text" : "password"}
                                placeholder={t("auth.confirmPasswordPlaceholder", "Confirm your password")}
                                value={confirmPassword}
                                onChange={(e) => setConfirmPassword(e.target.value)}
                            />
                            <button
                                type="button"
                                className="password-toggle"
                                onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                                aria-label={showConfirmPassword ? t("auth.hidePassword", "Hide Password") : t("auth.showPassword", "Show Password")}
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
                            {t("auth.termsAgreement", "I agree to the")}{" "}
                            <a href="/terms">{t("auth.termsOfUse", "Terms  of Use")}</a>{" "}
                            {t("auth.and", "and")}{" "}
                            <a href="/privacy">{t("auth.privacyPolicy", "Privacy Policy")}</a>
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
                    {isLoading ? t("auth.creatingAccount", "Creating Account...") : t("auth.registerButton", "Create Account")}
                </button>
            </form>
        </div>
    );
};

export default RegisterForm;
