import LoginForm from "../../components/auth/LoginForm";
import logo from "../../assets/images/logo.svg";
import "../../styles/auth.css";
import { useSearchParams } from "react-router-dom";
import { useTranslation } from "react-i18next";

const Login = () => {
    const { t } = useTranslation();

    const [searchParams] = useSearchParams();
    const googleError = searchParams.get("error") === "google";
    const sessionExpired = searchParams.get("expired") === "1";

    return (
        <div className="auth-page login">

            <div className="auth-brand-panel">
                <div className="auth-brand-content">
                    <div className="auth-logo">
                        <img src={logo} alt="Hunter Exam Trainer" />
                    </div>
                    <h1>JÄGAREXAMEN TRÄNARE</h1>
                    <p>
                        {t("auth.brandTagLine", "Det bästa sättet att förbereda sig för jägarexamen.")}
                    </p>
                </div>
            </div>

            {googleError && (
                <div className="auth-error-banner">
                    {t("auth.googleError", "Inloggning med Google misslyckades. Försök igen eller använd en annan metod.")}
                </div>
            )}

            {sessionExpired && (
                <div className="auth-error-banner">
                    {t("auth.sessionExpired", "Din session har gått ut. Vänligen logga in igen.")}
                </div>
            )}

            <LoginForm />
        </div>
    );
};

export default Login;
