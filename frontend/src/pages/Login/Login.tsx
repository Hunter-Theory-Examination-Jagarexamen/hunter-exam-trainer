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
    const notice = googleError ? t("auth.googleError")
        : sessionExpired ? t("auth.sessionExpired")
        : null;

    return (
        <div className="auth-page login">

            <div className="auth-brand-panel">
                <div className="auth-brand-content">
                    <div className="auth-logo">
                        <img src={logo} alt={t("brand.logoAlt")} />
                    </div>
                    <h1>{t("brand.title")}</h1>
                    <p>
                        {t("auth.brandTagLine")}
                    </p>
                </div>
            </div>

            {/* In the form, not floating at the top of the screen, so the
                server status banner can't cover it (#188). */}
            <LoginForm notice={notice} />
        </div>
    );
};

export default Login;
