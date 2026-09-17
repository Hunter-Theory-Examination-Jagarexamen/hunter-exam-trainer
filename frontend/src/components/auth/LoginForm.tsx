import {useState} from "react";
import { useNavigate } from "react-router-dom";
import {Eye, EyeOff, Lock, Mail} from "lucide-react";
import logo from "../../assets/images/logo.svg";
import "../../styles/auth.css";
import {loginUser} from "../../services/authService.ts";

const LoginForm = () => {

    const navigate = useNavigate();

    const [showPassword, setShowPassword] = useState(false);

    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [isLoading, setIsLoading] = useState(false);

    const handleLogin = async (e: React.FormEvent) => {
        e.preventDefault();

        if(!email || !password) {
            alert("Please enter your email and password");
            return;
        }

        try {
            setIsLoading(true);

            const response = await loginUser({
                email,
                password,
            });

            localStorage.setItem("token", response.token);

            navigate("/dashboard");

        } catch (error) {
            console.error("Login/API error:", error);
            alert("Invalid email or password");
        } finally {
            setIsLoading(false);
        }

    };

    return (
        <div className="auth-form-container">

            <div className="auth-form-header">
                <div className="auth-mobile-logo">
                    <img src={logo} alt="Hunter Exam Trainer" />
                </div>
                <h2>Welcome back!</h2>
                <p>Log in to continue your practice.</p>
            </div>

            <form onSubmit={handleLogin}>
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

                <div className="form-field">
                    <label htmlFor="password">
                        Password
                    </label>

                    <div className="input-wrapper">

                        <Lock size={18} />
                        <input
                            id="password"
                            type={showPassword ? "text" : "password"}
                            placeholder="Enter your password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                        />

                        <button
                            type="button"
                            className="password-toggle"
                            onClick={() => setShowPassword(!showPassword)}
                            aria-label={showPassword ? "Hide Password" : "Show Password"}
                        >
                            {showPassword ? <EyeOff size={18} /> : <Eye size={18} /> }
                        </button>

                    </div>

                </div>

                <div className="forgot-password">
                    <button type="button">
                        Forgot Password?
                    </button>
                </div>

                <button
                    type="submit"
                    className="auth-primary-button"
                    disabled={isLoading}
                >
                    {isLoading ? "Logging in..." : "Log In"}
                </button>
            </form>

            <div className="auth-divider">
                <span>or</span>
            </div>

            <div className="auth-secondary-actions">
                <button
                    type="button"
                    className="auth-secondary-button"
                >
                    Continue with Google
                </button>
                <button
                    type="button"
                    className="auth-secondary-button"
                >
                    Continue with Guest
                </button>
            </div>

            <p className="auth-switch">
                Don't have an account?{" "}
                <button
                    type="button"
                    onClick={() => navigate("/register")}
                >
                    Sign Up
                </button>
            </p>
        </div>
    );
};

export default LoginForm;