import "../../styles/profile.css";
import {useEffect, useState} from "react";
import apiClient from "../../api/apiClient.ts";
import type { User } from "../../types/user.ts";

const UserInformation = () => {

    const [user, setUser] = useState<User>();
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {

        const fetchUserInfo = async () => {
            try {
                setIsLoading(true);
                setError("");

                const data: User = await apiClient("/api/users/me");
                setUser(data);
            }
            catch (error) {
                console.error("Failed to load user details:", error);
                setError("Kunde inte ladda användardetaljer. Försök igen");
            }
            finally {
                setIsLoading(false);
            }
        };
        void fetchUserInfo();
    }, []);

    if (isLoading) {
        return <p>Laddar användardetaljer...</p>;
    }

    if (error) {
        return <p>{error}</p>;
    }

    if (!user) {
        return <p>Användardetaljer är inte tillgängliga.</p>;
    }

    return (
        <section className="profile-section">
            <h2>Användarinformation</h2>

            <div className="profile-card">
                <div className="profile-row">
                    <span>Namn</span>
                    <strong>{user.fullName}</strong>
                </div>
                <div className="profile-row">
                    <span>E-post</span>
                    <strong>{user.email}</strong>
                </div>
                <div className="profile-row">
                    <span>Roll</span>
                    <strong>{user.role}</strong>
                </div>
                <div className="profile-row">
                    <span>Medlem sedan</span>
                    <strong>
                        {new Date(user.createdAt).toLocaleDateString(
                            "sv-SE",
                            {
                                month: "long",
                                year: "numeric"
                            }
                        )}
                    </strong>
                </div>
            </div>
        </section>
    );
};

export default UserInformation;
