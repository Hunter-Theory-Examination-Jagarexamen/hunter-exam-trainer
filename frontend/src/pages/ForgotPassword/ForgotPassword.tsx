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
            alert("Vänligen ange din e-postadress");
            return;
        }

        try {
            setIsLoading(true);
            setError("");
            setMessage("");

            const response = await forgotPassword(email);

            setMessage(response.message);

        } catch (error) {
            setError(error instanceof Error ? error.message : "Något gick fel. Försök igen.");
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
                    <h1>JÄGAREXAMEN TRÄNARE</h1>
                    <p>
                        Återställ ditt lösenord för att fortsätta träna.
                    </p>
                </div>
            </div>

            <div className="auth-form-container">

                <div className="auth-form-header">
                    <div className="auth-mobile-logo">
                        <img src={logo} alt="Hunter Exam Trainer" />
                    </div>
                    <h2>Glömt lösenord</h2>
                    <p>
                        Ange din e-postadress för att få en länk för återställning av lösenord.
                    </p>
                </div>

                <form onSubmit={handleSubmit}>
                    <div className="form-field">
                        <label htmlFor="email">
                            E-post
                        </label>

                        <div className="input-wrapper">
                            <Mail size={18} />
                            <input
                                id="email"
                                type="email"
                                required
                                autoComplete="email"
                                placeholder="Ange din e-postadress"
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
                        {isLoading ? "Skickar..." : "Skicka återställningslänk"}
                    </button>
                </form>

                {error && <p role="alert" style={{color: "#e53e3e"}}>{error}</p>}

                {message && (
                    <div className="forgot-message" role="status">
                        <p>{message}</p>
                    </div>
                )}

                <p className="auth-switch">
                    Kom du ihåg ditt lösenord?{" "}
                    <button
                        type="button"
                        onClick={() => navigate("/login")}
                    >
                        Tillbaka till inloggning
                    </button>
                </p>

            </div>
        </div>
    );
};

export default ForgotPassword;
