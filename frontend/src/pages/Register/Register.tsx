import RegisterForm from "../../components/auth/RegisterForm";
import logo from "../../assets/images/logo.svg";
import { useTranslation } from "react-i18next";

const Register = () => {
    const { t } = useTranslation();

    return (
        <div className="auth-page register">

            <div className="auth-brand-panel">
                <div className="auth-brand-content">
                    <div className="auth-logo">
                        <img src={logo} alt={t("brand.logoAlt")} />
                    </div>
                    <h1>{t("brand.title")}</h1>
                    <p>
                        {t("auth.registerTagLine")}
                    </p>
                </div>
            </div>

            <RegisterForm />

        </div>
    );
};

export default Register;
