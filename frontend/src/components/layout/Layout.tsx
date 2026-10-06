import Header from "./Header";
import Sidebar from "./Sidebar";
import "../../styles/layout.css"
import {Outlet} from "react-router-dom";
import {useEffect, useState} from "react";
import apiClient from "../../api/apiClient";
import type {CurrentUserState, User} from "../../types/user";

const Layout = () => {
    const [currentUser, setCurrentUser] = useState<CurrentUserState>({
        user: null, isLoading: true, error: "",
    });

    useEffect(() => {
        let active = true;
        apiClient("/api/users/me").then((user: User) => {
            if (active) setCurrentUser({user, isLoading: false, error: ""});
        }).catch(() => {
            if (active) setCurrentUser({
                user: null, isLoading: false,
                error: "Failed to load user details. Please try again.",
            });
        });
        return () => { active = false; };
    }, []);

    return (
        <div className="layout">
            <Header user={currentUser.user} />

            <div className="layout-body">
                <Sidebar user={currentUser.user} />

                <main className="main-content">
                    <Outlet context={currentUser} />
                </main>
            </div>
        </div>
    );
};

export default Layout;