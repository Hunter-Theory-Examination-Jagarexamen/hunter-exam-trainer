import "../../styles/profile.css";
import {useEffect, useState} from "react";
import {useTranslation} from "react-i18next";
import apiClient from "../../api/apiClient.ts";
import type { User } from "../../types/user.ts";

const UserInformation = () => {
    const { t } = useTranslation();

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
                setError(t("profile.loadUserError"));
            }
            finally {
                setIsLoading(false);
            }
        };
        void fetchUserInfo();
    }, [t]);

    if (isLoading) {
        return <p>{t("profile.loadingUser")}</p>;
    }

    if (error) {
        return <p>{error}</p>;
    }

    if (!user) {
        return <p>{t("profile.userNotAvailable")}</p>;
    }

    return (
        <section className="profile-section">
            <h2>{t("profile.userInformationTitle")}</h2>

            <div className="profile-card">
                <div className="profile-row">
                    <span>{t("profile.nameLabel")}</span>
                    <strong>{user.fullName}</strong>
                </div>
                <div className="profile-row">
                    <span>{t("profile.emailLabel")}</span>
                    <strong>{user.email}</strong>
                </div>
                <div className="profile-row">
                    <span>{t("profile.roleLabel")}</span>
                    <strong>{user.role}</strong>
                </div>
                <div className="profile-row">
                    <span>{t("profile.memberSinceLabel")}</span>
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
