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
    const [newPassword, setNewPassword] = useState("");
    const [isLoading, setIsLoading] = useState(false);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!email) {
            alert("Please enter your email");
            return;
        }

        try {
            setIsLoading(true);

            const response = await forgotPassword(email);

            setMessage(response.message);
            setNewPassword(response.newPassword ?? "");

        } catch (error) {
            console.error("Forgot password error:", error);
            alert("Something went wrong. Please try again.");
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
                        Enter your email to generate a new password.
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
                        {isLoading ? "Generating..." : "Generate New Password"}
                    </button>
                </form>

                {message && (
                    <div className="forgot-message">
                        <p>{message}</p>

                        {newPassword && (
                            <div className="forgot-new-password">
                                <span>Your new password:</span>
                                <strong>{newPassword}</strong>
                                <p>
                                    Use it to log in, then change it from your profile.
                                </p>
                            </div>
                        )}
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