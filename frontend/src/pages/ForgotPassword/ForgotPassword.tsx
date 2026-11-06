import {useState} from "react";
import {useNavigate} from "react-router-dom";
import {useTranslation} from "react-i18next";
import {Mail} from "lucide-react";
import logo from "../../assets/images/logo.svg";
import "../../styles/auth.css";
import {forgotPassword} from "../../services/authService.ts";

const ForgotPassword = () => {
    const {t} = useTranslation();
    const navigate = useNavigate();

    const [email, setEmail] = useState("");
    const [message, setMessage] = useState("");
    const [error, setError] = useState("");
    const [isLoading, setIsLoading] = useState(false);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!email) {
            alert(t("forgotPassword.emptyEmailAlert"));
            return;
        }

        try {
            setIsLoading(true);
            setError("");
            setMessage("");

            const response = await forgotPassword(email);

            setMessage(response.message);

        } catch (error) {
            setError(error instanceof Error ? error.message : t("forgotPassword.defaultError"));
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="auth-page login">

            <div className="auth-brand-panel">
                <div className="auth-brand-content">
                    <div className="auth-logo">
                        <img src={logo} alt="Hunter Exam Trainer" />
                    </div>
                    <h1>{t("forgotPassword.brandTitle")}</h1>
                    <p>
                        {t("forgotPassword.brandSubtitle")}
                    </p>
                </div>
            </div>

            <div className="auth-form-container">

                <div className="auth-form-header">
                    <div className="auth-mobile-logo">
                        <img src={logo} alt="Hunter Exam Trainer" />
                    </div>
                    <h2>{t("forgotPassword.title")}</h2>
                    <p>
                        {t("forgotPassword.subtitle")}
                    </p>
                </div>

                <form onSubmit={handleSubmit}>
                    <div className="form-field">
                        <label htmlFor="email">
                            {t("forgotPassword.emailLabel")}
                        </label>

                        <div className="input-wrapper">
                            <Mail size={18} />
                            <input
                                id="email"
                                type="email"
                                required
                                autoComplete="email"
                                placeholder={t("forgotPassword.emailPlaceholder")}
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                            />
                        </div>
                    </div>

                    <button
                        type="submit"
                        className="auth-primary-button"
                        disabled={isLoading}
                    >
                        {isLoading ? t("forgotPassword.submittingButton") : t("forgotPassword.submitButton")}
                    </button>
                </form>

                {error && <p role="alert" style={{color: "#e53e3e"}}>{error}</p>}

                {message && (
                    <div className="forgot-message" role="status">
                        <p>{message}</p>
                    </div>
                )}

                <p className="auth-switch">
                    {t("forgotPassword.rememberPassword")}{" "}
                    <button
                        type="button"
                        onClick={() => navigate("/login")}
                    >
                        {t("forgotPassword.backToLogin")}
                    </button>
                </p>

            </div>
        </div>
    );
};

export default ForgotPassword;
