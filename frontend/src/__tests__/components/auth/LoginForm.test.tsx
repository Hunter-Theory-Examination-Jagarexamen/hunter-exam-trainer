// Tests for src/components/auth/LoginForm.tsx

import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter, Routes, Route } from "react-router-dom";
import LoginForm from "../../../components/auth/LoginForm";
import { checkGoogleLoginEnabled, loginUser } from "../../../services/authService.ts";

// Replace the real authService (which calls the backend) with mock functions.
// checkGoogleLoginEnabled must return a Promise, because LoginForm calls .then() on it.
vi.mock("../../../services/authService.ts", () => ({
    checkGoogleLoginEnabled: vi.fn().mockResolvedValue(false),
    loginUser: vi.fn(),
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
        vi.restoreAllMocks(); // undo vi.spyOn (console.error)
        localStorage.clear(); // remove any saved token
    });

    it("hides the Google button when Google login is not configured", async () => {
        // Arrange: the mock at the top answers "not configured" (false)
        renderLoginForm();

        // Assert: wait until the check has run, then the button should be missing
        await waitFor(() => expect(checkGoogleLoginEnabled).toHaveBeenCalled());
        expect(screen.queryByRole("button", { name: "Continue with Google" })).not.toBeInTheDocument();
    });

    it("shows the Google button when Google login is configured", async () => {
        // Arrange: this time the check answers "configured" (true), for one call only
        vi.mocked(checkGoogleLoginEnabled).mockResolvedValueOnce(true);
        renderLoginForm();

        // Assert: findByRole waits until the button appears (or times out)
        expect(await screen.findByRole("button", { name: "Continue with Google" })).toBeInTheDocument();
    });

    it("shows an error message and does not log in when the fields are empty", async () => {
        // Arrange
        renderLoginForm();

        // Act: click "Log In" without typing anything
        await userEvent.click(screen.getByRole("button", { name: "Log In" }));

        // Assert: the user is told what is missing, and the backend is never called
        // role="alert" is how screen readers (and this test) find the error message
        expect(screen.getByRole("alert")).toHaveTextContent("Please enter your email and password");
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

    it("shows an error message and saves no token when the login fails", async () => {
        // Arrange: the fake backend rejects the login
        vi.mocked(loginUser).mockRejectedValue(new Error("401"));
        vi.spyOn(console, "error").mockImplementation(() => {}); // keep test output clean
        renderLoginForm();

        // Act
        await userEvent.type(screen.getByLabelText("Email"), "anna@example.com");
        await userEvent.type(screen.getByLabelText("Password"), "wrong-password");
        await userEvent.click(screen.getByRole("button", { name: "Log In" }));

        // Assert: findByRole waits until the error message has appeared (or times out)
        expect(await screen.findByRole("alert")).toHaveTextContent("Invalid email or password");
        expect(localStorage.getItem("token")).toBeNull();
    });

    it("shows the account creation success message when navigated from registration", async () => {
        // Arrange: render with initialEntries that include state
        render (
            <MemoryRouter initialEntries={[{ pathname: "/login", state: { accountCreated: true } }]}>
                <Routes>
                    <Route path="/login" element={<LoginForm />} />
                </Routes>
            </MemoryRouter>
        );

        // Assert: the success message is shown
        expect(await screen.findByRole("status")).toHaveTextContent("Account created successfully! You can now log in.");
    });

    it("does not show the account creation success message when opened directly", async () => {
        // Arrange: render normally without state
        renderLoginForm();

        // Assert: no status role message should be present
        expect(screen.queryByRole("status")).not.toBeInTheDocument();
    });

});
