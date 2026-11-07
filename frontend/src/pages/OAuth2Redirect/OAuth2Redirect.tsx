import {useEffect} from "react";
import {useNavigate, useSearchParams} from "react-router-dom";
import {useTranslation} from "react-i18next";

const OAuth2Redirect = () => {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();

    const token = searchParams.get("token");

    useEffect(() => {

        if (token) {
            localStorage.setItem("token", token);
            navigate("/dashboard", {replace: true});
            return;
        }

        navigate("/login", {replace: true});

    }, [token, navigate]);

    return (
        <div className="auth-page login">
            <div className="auth-form-container">
                <p className="auth-switch">{t("auth.signingIn")}</p>
            </div>
        </div>
    );
};

export default OAuth2Redirect;
