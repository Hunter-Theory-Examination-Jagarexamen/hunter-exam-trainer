import Header from "./Header";
import Sidebar from "./Sidebar";
import "../../styles/layout.css";
import { Outlet, useLocation } from "react-router-dom";
import { useEffect, useState } from "react";
import apiClient from "../../api/apiClient";
import type { CurrentUserState, User } from "../../types/user";

const Layout = () => {
    const [currentUser, setCurrentUser] = useState<CurrentUserState>({
        user: null, isLoading: true, error: "",
    });

    const [isMobileNavOpen, setIsMobileNavOpen] = useState(false);

    const location = useLocation();

    // Auto-close the mobile nav when the route changes.
    useEffect(() => {
        setIsMobileNavOpen(false);
    }, [location.pathname]);

    useEffect(() => {
        let active = true;
        apiClient("/api/users/me").then((user: User) => {
            if (active) setCurrentUser({ user, isLoading: false, error: "" });
        }).catch(() => {
            if (active) setCurrentUser({
                user: null,
                isLoading: false,
                error: "Failed to load user details. Please try again.",
            });
        });
        return () => { active = false; };
    }, []);

    return (
        <div className="layout">
            <Header
                user={currentUser.user}
                onMenuToggle={() => setIsMobileNavOpen((open) => !open)}
            />

            <div className="layout-body">
                <Sidebar
                    user={currentUser.user}
                    isOpen={isMobileNavOpen}
                    onClose={() => setIsMobileNavOpen(false)}
                />

                {isMobileNavOpen && (
                    <div
                        className="sidebar-backdrop"
                        onClick={() => setIsMobileNavOpen(false)}
                        aria-hidden="true"
                    />
                )}

                <main className="main-content">
                    <Outlet context={currentUser} />
                </main>
            </div>
        </div>
    );
};

export default Layout;