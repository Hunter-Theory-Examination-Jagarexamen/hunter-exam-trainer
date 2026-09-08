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
                setError("Failed to load User details. Please try again");
            }
            finally {
                setIsLoading(false);
            }
        };
        void fetchUserInfo();
    }, []);

    if (isLoading) {
        return <p>Loading User details...</p>;
    }

    if (error) {
        return <p>{error}</p>;
    }

    if (!user) {
        return <p>User details not available.</p>;
    }

    return (
        <section className="profile-section">
            <h2>User Information</h2>

            <div className="profile-card">
                <div className="profile-row">
                    <span>Name</span>
                    <strong>{user.fullName}</strong>
                </div>
                <div className="profile-row">
                    <span>Email</span>
                    <strong>{user.email}</strong>
                </div>
                <div className="profile-row">
                    <span>Role</span>
                    <strong>{user.role}</strong>
                </div>
                <div className="profile-row">
                    <span>Member Since</span>
                    <strong>
                        {new Date(user.createdAt).toLocaleDateString(
                            "en-US",
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