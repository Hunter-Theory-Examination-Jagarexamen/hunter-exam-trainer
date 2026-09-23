import LoginForm from "../../components/auth/LoginForm";
import logo from "../../assets/images/logo.svg";
import "../../styles/auth.css";
import { useSearchParams } from "react-router-dom";

const Login = () => {

    const [searchParams] = useSearchParams();
    const googleError = searchParams.get("error") === "google";

    return (
        <div className="auth-page login">

            <div className="auth-brand-panel">
                <div className="auth-brand-content">
                    <div className="auth-logo">
                        <img src={logo} alt="Hunter Exam Trainer" />
                    </div>
                    <h1>HUNTER EXAM TRAINER</h1>
                    <p>
                        The best way to prepare for the hunter exam.
                    </p>
                </div>
            </div>

            {googleError && (
                <div className="auth-error-banner">
                    Google sign-in failed. Try again or use another method.
                </div>
            )}

            <LoginForm />
        </div>
    );
};

export default Login;