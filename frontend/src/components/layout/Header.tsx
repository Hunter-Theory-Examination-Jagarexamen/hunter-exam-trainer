import "../../styles/header.css"
import logo from "../../assets/images/logo.svg";
import {useEffect, useState} from "react";
import type {User} from "../../types/user.ts";
import apiClient from "../../api/apiClient.ts";

const Header = () => {

    const [user, setUser] = useState<User | null>(null);

    useEffect(() => {

        const fetchUserInfo = async () => {
            try {
                const data: User = await apiClient("/api/users/me");
                setUser(data);
            }
            catch (error) {
                console.error("Failed to load user details:", error);
            }
        };
        void fetchUserInfo();
    }, []);

    return (
        <header className="header">
            <div className="header-left">
                <img
                    src={logo}
                    alt="Hunter Exam Trainer Logo"
                    className="header-logo"
                />
                <h1>Hunter Theory Exam</h1>
            </div>

            <div className="header-right">
                <span>Welcome, {user ? user.fullName : "User"}</span>
            </div>
        </header>
    );
};

export default Header;