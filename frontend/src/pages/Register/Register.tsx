import RegisterForm from "../../components/auth/RegisterForm";
import logo from "../../assets/images/logo.svg";

const Register = () => {
    return (
        <div className="auth-page register">

            <div className="auth-brand-panel">
                <div className="auth-brand-content">
                    <div className="auth-logo">
                        <img src={logo} alt="Hunter Exam Trainer" />
                    </div>
                    <h1>JÄGAREXAMEN TRÄNARE</h1>
                    <p>
                        Skapa ett konto för att följa dina framsteg
                        och förbättra dina resultat.
                    </p>
                </div>
            </div>

            <RegisterForm />

        </div>
    );
};

export default Register;
