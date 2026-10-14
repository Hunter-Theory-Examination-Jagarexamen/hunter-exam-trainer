import {useState} from "react";
import {useNavigate} from "react-router-dom";
import {Mail} from "lucide-react";
import logo from "../../assets/images/logo.svg";
import "../../styles/auth.css";
import {forgotPassword} from "../../services/authService.ts";

const ForgotPassword = () => {

    const navigate = useNavigate();

    const [email, setEmail] = useState("");
    const [message, setMessage] = useState("");
    const [error, setError] = useState("");
    const [isLoading, setIsLoading] = useState(false);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!email) {
            alert("Please enter your email");
            return;
        }

        try {
            setIsLoading(true);
            setError("");
            setMessage("");

            const response = await forgotPassword(email);

            setMessage(response.message);

        } catch (error) {
            setError(error instanceof Error ? error.message : "Something went wrong. Please try again.");
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
                    <h1>HUNTER EXAM TRAINER</h1>
                    <p>
                        Reset your password to continue your practice.
                    </p>
                </div>
            </div>

            <div className="auth-form-container">

                <div className="auth-form-header">
                    <div className="auth-mobile-logo">
                        <img src={logo} alt="Hunter Exam Trainer" />
                    </div>
                    <h2>Forgot password</h2>
                    <p>
                        Enter your email to receive a password reset link.
                    </p>
                </div>

                <form onSubmit={handleSubmit}>
                    <div className="form-field">
                        <label htmlFor="email">
                            Email
                        </label>

                        <div className="input-wrapper">
                            <Mail size={18} />
                            <input
                                id="email"
                                type="email"
                                required
                                autoComplete="email"
                                placeholder="Enter your email"
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
                        {isLoading ? "Sending..." : "Send Reset Link"}
                    </button>
                </form>

                {error && <p role="alert" style={{color: "#e53e3e"}}>{error}</p>}

                {message && (
                    <div className="forgot-message" role="status">
                        <p>{message}</p>
                    </div>
                )}

                <p className="auth-switch">
                    Remembered your password?{" "}
                    <button
                        type="button"
                        onClick={() => navigate("/login")}
                    >
                        Back to Login
                    </button>
                </p>

            </div>
        </div>
    );
};

export default ForgotPassword;
