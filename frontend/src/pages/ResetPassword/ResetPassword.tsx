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
            setError("Lösenorden matchar inte.");
            return;
        }
        if (password.length < 8 || new TextEncoder().encode(password).length > 72) {
            setError("Lösenordet måste vara minst 8 tecken och högst 72 UTF-8-byte.");
            return;
        }
        setIsLoading(true);
        try {
            const response = await resetPassword(token, password);
            setMessage(response.message);
            setPassword("");
            setConfirmPassword("");
        } catch (err) {
            setError(err instanceof Error ? err.message : "Kunde inte återställa lösenordet. Försök igen.");
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="auth-page login">
            <div className="auth-brand-panel">
                <div className="auth-brand-content">
                    <div className="auth-logo"><img src={logo} alt="Hunter Exam Trainer" /></div>
                    <h1>JÄGAREXAMEN TRÄNARE</h1>
                    <p>Återställ ditt lösenord för att fortsätta träna.</p>
                </div>
            </div>
            <div className="auth-form-container">
                <div className="auth-form-header">
                    <div className="auth-mobile-logo"><img src={logo} alt="Hunter Exam Trainer" /></div>
                    <h2>Återställ lösenord</h2>
                    <p>Välj ett nytt lösenord på minst 8 tecken.</p>
                </div>
                {!validToken && <p role="alert">Återställningslänken är ogiltig. Vänligen begär en ny återställningslänk.</p>}
                {validToken && !message && (
                    <form onSubmit={handleSubmit}>
                        <div className="form-field">
                            <label htmlFor="password">Nytt lösenord</label>
                            <div className="input-wrapper">
                                <Lock size={18} />
                                <input id="password" type="password" autoComplete="new-password"
                                       required minLength={8} value={password}
                                       onChange={(e) => setPassword(e.target.value)} />
                            </div>
                        </div>
                        <div className="form-field">
                            <label htmlFor="confirmPassword">Bekräfta nytt lösenord</label>
                            <div className="input-wrapper">
                                <Lock size={18} />
                                <input id="confirmPassword" type="password" autoComplete="new-password"
                                       required minLength={8} value={confirmPassword}
                                       onChange={(e) => setConfirmPassword(e.target.value)} />
                            </div>
                        </div>
                        {error && <p role="alert" style={{color: "#e53e3e"}}>{error}</p>}
                        <button type="submit" className="auth-primary-button" disabled={isLoading}>
                            {isLoading ? "Återställer..." : "Återställ lösenord"}
                        </button>
                    </form>
                )}
                {message && <div className="forgot-message" role="status">{message}</div>}
                {!message && <p className="auth-switch"><button type="button" onClick={() => navigate("/forgot-password")}>Begär en ny återställningslänk</button></p>}
                <p className="auth-switch"><button type="button" onClick={() => navigate("/login")}>Tillbaka till inloggning</button></p>
            </div>
        </div>
    );
};

export default ResetPassword;
