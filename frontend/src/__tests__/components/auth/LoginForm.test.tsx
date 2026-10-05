// Tests for src/components/auth/LoginForm.tsx

import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter, Routes, Route } from "react-router-dom";
import LoginForm from "../../../components/auth/LoginForm";
import { loginUser } from "../../../services/authService.ts";

// Replace the real authService (which calls the backend) with mock functions.
// checkGoogleLoginEnabled must return a Promise, because LoginForm calls .then() on it.
vi.mock("../../../services/authService.ts", () => ({
    checkGoogleLoginEnabled: vi.fn().mockResolvedValue(false),
    loginUser: vi.fn(),
    loginAsGuest: vi.fn(),
}));

// Draw the form inside a test router. The fake /dashboard page lets us
// see whether a successful login navigates there.
function renderLoginForm() {
    render(
        <MemoryRouter initialEntries={["/login"]}>
            <Routes>
                <Route path="/login" element={<LoginForm />} />
                <Route path="/dashboard" element={<p>Dashboard page</p>} />
            </Routes>
        </MemoryRouter>
    );
}

describe("LoginForm", () => {

    beforeEach(() => {
        vi.clearAllMocks();   // forget calls recorded by earlier tests
    });

    afterEach(() => {
        vi.restoreAllMocks(); // undo vi.spyOn (alert, console.error)
        localStorage.clear(); // remove any saved token
    });

    it("shows an alert and does not log in when the fields are empty", async () => {
        // Arrange: catch alert() calls instead of showing a real pop-up
        const alertSpy = vi.spyOn(window, "alert").mockImplementation(() => {});
        renderLoginForm();

        // Act: click "Log In" without typing anything
        await userEvent.click(screen.getByRole("button", { name: "Log In" }));

        // Assert: the user is told what is missing, and the backend is never called
        expect(alertSpy).toHaveBeenCalledWith("Please enter your email and password");
        expect(loginUser).not.toHaveBeenCalled();
    });

    it("saves the token and goes to the dashboard after a successful login", async () => {
        // Arrange: the fake backend accepts the login and returns a token
        vi.mocked(loginUser).mockResolvedValue({ token: "test-token", tokenType: "Bearer" });
        renderLoginForm();

        // Act: fill in the form and log in
        await userEvent.type(screen.getByLabelText("Email"), "anna@example.com");
        await userEvent.type(screen.getByLabelText("Password"), "secret123");
        await userEvent.click(screen.getByRole("button", { name: "Log In" }));

        // Assert: the backend got what was typed, the token is saved, and the dashboard shows
        expect(loginUser).toHaveBeenCalledWith({ email: "anna@example.com", password: "secret123" });
        expect(await screen.findByText("Dashboard page")).toBeInTheDocument();
        expect(localStorage.getItem("token")).toBe("test-token");
    });

    it("shows an alert and saves no token when the login fails", async () => {
        // Arrange: the fake backend rejects the login
        vi.mocked(loginUser).mockRejectedValue(new Error("401"));
        const alertSpy = vi.spyOn(window, "alert").mockImplementation(() => {});
        vi.spyOn(console, "error").mockImplementation(() => {}); // keep test output clean
        renderLoginForm();

        // Act
        await userEvent.type(screen.getByLabelText("Email"), "anna@example.com");
        await userEvent.type(screen.getByLabelText("Password"), "wrong-password");
        await userEvent.click(screen.getByRole("button", { name: "Log In" }));

        // Assert: waitFor retries until the alert has appeared (or times out)
        await waitFor(() => expect(alertSpy).toHaveBeenCalledWith("Invalid email or password"));
        expect(localStorage.getItem("token")).toBeNull();
    });
});
