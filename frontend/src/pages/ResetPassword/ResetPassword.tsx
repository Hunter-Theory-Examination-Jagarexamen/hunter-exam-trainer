import {useEffect, useState} from "react";
import {useNavigate} from "react-router-dom";
import {Lock} from "lucide-react";
import logo from "../../assets/images/logo.svg";
import "../../styles/auth.css";
import {resetPassword} from "../../services/authService";

const ResetPassword = () => {
    const navigate = useNavigate();
    const [token] = useState(() => new URLSearchParams(window.location.hash.slice(1)).get("token") ?? "");
    const [password, setPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [error, setError] = useState("");
    const [message, setMessage] = useState("");
    const [isLoading, setIsLoading] = useState(false);
    const validToken = /^[A-Za-z0-9_-]{43}$/.test(token);

    useEffect(() => {
        // Keep the bearer reset token out of the address bar and browser history.
        window.history.replaceState(window.history.state, "", window.location.pathname);
    }, []);

    const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        setError("");
        if (password !== confirmPassword) {
            setError("Passwords do not match.");
            return;
        }
        if (password.length < 8 || new TextEncoder().encode(password).length > 72) {
            setError("Password must be at least 8 characters and at most 72 UTF-8 bytes.");
            return;
        }
        setIsLoading(true);
        try {
            const response = await resetPassword(token, password);
            setMessage(response.message);
            setPassword("");
            setConfirmPassword("");
        } catch (err) {
            setError(err instanceof Error ? err.message : "Unable to reset password. Please try again.");
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="auth-page login">
            <div className="auth-brand-panel">
                <div className="auth-brand-content">
                    <div className="auth-logo"><img src={logo} alt="Hunter Exam Trainer" /></div>
                    <h1>HUNTER EXAM TRAINER</h1>
                    <p>Reset your password to continue your practice.</p>
                </div>
            </div>
            <div className="auth-form-container">
                <div className="auth-form-header">
                    <div className="auth-mobile-logo"><img src={logo} alt="Hunter Exam Trainer" /></div>
                    <h2>Reset password</h2>
                    <p>Choose a new password of at least 8 characters.</p>
                </div>
                {!validToken && <p role="alert">Reset link is invalid. Please request a new reset link.</p>}
                {validToken && !message && (
                    <form onSubmit={handleSubmit}>
                        <div className="form-field">
                            <label htmlFor="password">New password</label>
                            <div className="input-wrapper">
                                <Lock size={18} />
                                <input id="password" type="password" autoComplete="new-password"
                                       required minLength={8} value={password}
                                       onChange={(e) => setPassword(e.target.value)} />
                            </div>
                        </div>
                        <div className="form-field">
                            <label htmlFor="confirmPassword">Confirm new password</label>
                            <div className="input-wrapper">
                                <Lock size={18} />
                                <input id="confirmPassword" type="password" autoComplete="new-password"
                                       required minLength={8} value={confirmPassword}
                                       onChange={(e) => setConfirmPassword(e.target.value)} />
                            </div>
                        </div>
                        {error && <p role="alert" style={{color: "#e53e3e"}}>{error}</p>}
                        <button type="submit" className="auth-primary-button" disabled={isLoading}>
                            {isLoading ? "Resetting..." : "Reset Password"}
                        </button>
                    </form>
                )}
                {message && <div className="forgot-message" role="status">{message}</div>}
                {!message && <p className="auth-switch"><button type="button" onClick={() => navigate("/forgot-password")}>Request a new reset link</button></p>}
                <p className="auth-switch"><button type="button" onClick={() => navigate("/login")}>Back to Login</button></p>
            </div>
        </div>
    );
};

export default ResetPassword;
