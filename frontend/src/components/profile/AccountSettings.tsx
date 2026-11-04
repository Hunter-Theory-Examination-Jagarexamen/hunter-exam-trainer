import "../../styles/profile.css";
import {Lock} from "lucide-react";
import {useState} from "react";
import * as React from "react";
import apiClient from "../../api/apiClient.ts";

const AccountSettings = () => {

    const [showChangePassword, setShowChangePassword] = useState(false);

    const [currentPassword, setCurrentPassword] = useState("");
    const [newPassword, setNewPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");

    const [message, setMessage] = useState("");
    const [error, setError] = useState("");
    const [isLoading, setIsLoading] = useState(false);

    const handleChangePassword = async (
        event: React.FormEvent<HTMLFormElement>
    ) => {
        event.preventDefault();

        setMessage("");
        setError("");

        if (newPassword !== confirmPassword) {
            setError("Det nya lösenordet och bekräftelsen matchar inte.");
            return;
        }

        try {
            setIsLoading(true);

            await apiClient("/api/users/me/password", {
                method: "PUT",
                body: JSON.stringify({
                    currentPassword,
                    newPassword
                })
            });
            setMessage("Lösenordet har ändrats");

            setCurrentPassword("");
            setNewPassword("");
            setConfirmPassword("");
        }
        catch (error) {
            console.error("Failed to change password:", error);
            setError(
                error instanceof Error
                    ? error.message
                    : "Kunde inte ändra lösenord. Försök igen."
            );
        }
        finally {
            setIsLoading(false);
        }
    };

    const handleCancel = () => {
        setShowChangePassword(false);
        setCurrentPassword("");
        setNewPassword("");
        setConfirmPassword("");
        setMessage("");
        setError("");
    };

    return (
        <section className="profile-section">
            <h2>Kontoinställningar</h2>

            <div className="account-settings">

                {!showChangePassword && (
                    <button
                        type="button"
                        onClick={() => setShowChangePassword(true)}
                    >
                        <Lock size={16}/>
                        Ändra lösenord
                    </button>
                )}

                {showChangePassword && (
                    <form
                        className="change-password-form"
                        onSubmit={handleChangePassword}
                    >
                        <div className="form-group">
                            <label htmlFor="currentPassword">
                                Nuvarande lösenord
                            </label>
                            <input
                                id="currentPassword"
                                type="password"
                                value={currentPassword}
                                onChange={(event) =>
                                    setCurrentPassword(event.target.value)
                                }
                                required
                            />
                        </div>

                        <div className="form-group">
                            <label htmlFor="newPassword">
                                Nytt lösenord
                            </label>
                            <input
                                id="newPassword"
                                type="password"
                                value={newPassword}
                                onChange={(event) =>
                                    setNewPassword(event.target.value)
                                }
                                required
                            />
                        </div>

                        <div className="form-group">
                            <label htmlFor="confirmPassword">
                                Bekräfta lösenord
                            </label>
                            <input
                                id="confirmPassword"
                                type="password"
                                value={confirmPassword}
                                onChange={(event) =>
                                    setConfirmPassword(event.target.value)
                                }
                                required
                            />
                        </div>

                        {error && (
                            <p className="password-error">
                                {error}
                            </p>
                        )}

                        {message && (
                            <p className="password-success">
                                {message}
                            </p>
                        )}

                        <div className="change-password-actions">
                            <button
                                type="button"
                                onClick={handleCancel}
                                disabled={isLoading}
                            >
                                Avbryt
                            </button>

                            <button
                                type="submit"
                                disabled={isLoading}
                            >
                                {isLoading ? "Ändrar..." : "Ändra lösenord"}
                            </button>
                        </div>
                    </form>
                )}
            </div>
        </section>
    );
};

export default AccountSettings;
