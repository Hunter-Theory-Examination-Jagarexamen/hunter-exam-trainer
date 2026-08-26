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
                    <h1>HUNTER EXAM TRAINER</h1>
                    <p>
                        Create an account to track your progress
                        and improve your results.
                    </p>
                </div>
            </div>

            <RegisterForm />

        </div>
    );
};

export default Register;