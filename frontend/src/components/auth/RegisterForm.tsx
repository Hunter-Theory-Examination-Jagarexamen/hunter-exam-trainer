import apiClient from "../../api/apiClient";
import "../../styles/auth.css";
import {useState} from "react";
import { useNavigate } from "react-router-dom";
import { Eye, EyeOff } from "lucide-react";
import logo from "../../assets/images/logo.svg";

const RegisterForm = () => {

    const navigate = useNavigate();

    const [showPassword, setShowPassword] = useState(false);
    const [showConfirmPassword, setShowConfirmPassword] = useState(false);

    const [fullName, setFullName] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [acceptedTerms, setAcceptedTerms] = useState(false);

    const [error, setError] = useState("");
    const [isLoading, setIsLoading] = useState(false);

    // Handles form submission for new user registration
    const handleRegister = async (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
    // Validates that all form fields are populated
        if (!fullName || !email || !password || !confirmPassword) {
            setError("Please fill in all the fields.");
            return;
        }
    // Validates that password matches confirm password
        if (password !== confirmPassword) {
            setError("Passwords do not match.");
            return;
        }
    // Validates minimum password length requirement of 8 characters
        if(password.length < 8) {
            setError("Password must be at least 8 characters long.");
            return;
        }
    // Validates that the user accepted terms and conditions
        if (!acceptedTerms) {
            setError("Please accept the Terms of Use and Privacy Policy.");
            return;
        }
    // Sets loading state to true during API request
        setIsLoading(true);

        try {
            await apiClient("/api/auth/register", {
                method: "POST",
                body: JSON.stringify({
                    fullName,
                    email,
                    password,
                }),
            });
    // Redirects user to login page after successful registration
            navigate("/login");
        } catch (err: any) {
            setError(err.message || "Registration failed. Please try again.");
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="auth-form-container register-form-container">

            <div className="auth-form-header">
                <div className="auth-mobile-logo">
                    <img src={logo} alt="Hunter Exam Trainer" />
                </div>

                <h2>Create Account</h2>

                <p className="">
                    Already have an account?{" "}
                    <button
                        type="button"
                        onClick={() => navigate("/login")}
                    >
                        Log In
                    </button>
                </p>
            </div>

            <form onSubmit={handleRegister}>
                <div className="register-fields">
                    {/* Full Name */}
                    <div className="form-field">
                        <label htmlFor="fullName">
                            Full Name
                        </label>
                        <div className="input-wrapper">
                            <input
                                id="fullName"
                                type="text"
                                placeholder="Enter your full name"
                                value={fullName}
                                onChange={(e) => setFullName(e.target.value)}
                            />
                        </div>
                    </div>

                    {/* Email */}
                    <div className="form-field">
                        <label htmlFor="email">
                            Email
                        </label>
                        <div className="input-wrapper">
                            <input
                                id="email"
                                type="email"
                                placeholder="Enter your email"
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                            />
                        </div>
                    </div>

                    {/* Password */}
                    <div className="form-field">
                        <label htmlFor="password">
                            Password
                        </label>
                        <div className="input-wrapper">
                            <input
                                id="password"
                                type={showPassword ? "text" : "password"}
                                placeholder="Create a password"
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

                    {/* Confirm Password */}
                    <div className="form-field">
                        <label htmlFor="confirmPassword">
                            Confirm Password
                        </label>
                        <div className="input-wrapper">
                            <input
                                id="confirmPassword"
                                type={showConfirmPassword ? "text" : "password"}
                                placeholder="Confirm your password"
                                value={confirmPassword}
                                onChange={(e) => setConfirmPassword(e.target.value)}
                            />
                            <button
                                type="button"
                                className="password-toggle"
                                onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                                aria-label={showConfirmPassword ? "Hide Password" : "Show Password"}
                            >
                                {showConfirmPassword ? <EyeOff size={18} /> : <Eye size={18} /> }
                            </button>
                        </div>
                    </div>
                </div>

                {/* Terms */}
                <div className="terms-field">
                    <label>
                        <input
                            type="checkbox"
                            name="terms"
                            checked={acceptedTerms}
                            onChange={(e) => setAcceptedTerms(e.target.checked)}
                        />
                        <span>
                            I agree to the{" "}
                            <a href="/terms">Terms of Use</a>{" "}
                            and{" "}
                            <a href="/privacy">Privacy Policy</a>
                        </span>
                    </label>
                </div>

                {error && (
                    <p style={{ color: "#e53e3e", marginBottom: "1rem", fontSize: "0.875rem" }}>
                        {error}
                    </p>
                )}

                {/* Submit */}
                <button
                    type="submit"
                    className="auth-primary-button"
                    disabled={isLoading}
                >
                    {isLoading ? "Creating Account..." : "Create Account"}
                </button>
            </form>
        </div>
    );
};

export default RegisterForm;