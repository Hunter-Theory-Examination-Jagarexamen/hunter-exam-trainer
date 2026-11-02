import {StrictMode} from "react";
import {afterEach, describe, expect, it} from "vitest";
import {render, screen} from "@testing-library/react";
import {MemoryRouter, Route, Routes, useLocation} from "react-router-dom";
import ProtectedRoute from "../../../components/auth/ProtectedRoute";
import {fakeToken} from "../../fakeToken";

// Shows the login address with its query string, e.g. "Login page ?expired=1".
function LoginProbe() {
    const {search} = useLocation();
    return <p>Login page {search}</p>;
}

function renderDashboard() {
    // StrictMode like main.tsx: React renders twice, which shows side effects during render.
    render(
        <StrictMode>
        <MemoryRouter initialEntries={["/dashboard"]}>
            <Routes>
                <Route path="/login" element={<LoginProbe />} />
                <Route element={<ProtectedRoute />}>
                    <Route path="/dashboard" element={<p>Dashboard page</p>} />
                </Route>
            </Routes>
        </MemoryRouter>
        </StrictMode>
    );
}

afterEach(() => localStorage.clear());

describe("ProtectedRoute", () => {
    it("shows the page with a valid login", () => {
        localStorage.setItem("token", fakeToken(3600));
        renderDashboard();
        expect(screen.getByText("Dashboard page")).toBeInTheDocument();
    });

    it("sends an expired login to the login page with the expired message", () => {
        localStorage.setItem("token", fakeToken(-60));
        renderDashboard();
        expect(screen.getByText("Login page ?expired=1")).toBeInTheDocument();
    });

    it("sends a user without a token to the login page", () => {
        renderDashboard();
        expect(screen.getByText("Login page")).toBeInTheDocument();
    });

    it("sends an unreadable token to the login page without the expired message", () => {
        localStorage.setItem("token", "not-a-jwt");
        renderDashboard();
        expect(screen.getByText("Login page")).toBeInTheDocument();
    });
});
