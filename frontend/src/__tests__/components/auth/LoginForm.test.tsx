// Tests for src/components/auth/LoginForm.tsx

import { describe, it, expect, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import LoginForm from "../../../components/auth/LoginForm";
import { loginUser } from "../../../services/authService.ts";

// Replace the real authService (which calls the backend) with mock functions.
// checkGoogleLoginEnabled must return a Promise, because LoginForm calls .then() on it.
vi.mock("../../../services/authService.ts", () => ({
    checkGoogleLoginEnabled: vi.fn().mockResolvedValue(false),
    loginUser: vi.fn(),
    loginAsGuest: vi.fn(),
}));

describe("LoginForm", () => {

    it("shows an alert and does not log in when the fields are empty", async () => {
        // Arrange: catch alert() calls instead of showing a real pop-up
        const alertSpy = vi.spyOn(window, "alert").mockImplementation(() => {});

        // LoginForm uses useNavigate(), so it must be inside a router
        render(
            <MemoryRouter>
                <LoginForm />
            </MemoryRouter>
        );

        // Act: click "Log In" without typing anything
        await userEvent.click(screen.getByRole("button", { name: "Log In" }));

        // Assert: the user is told what is missing, and the backend is never called
        expect(alertSpy).toHaveBeenCalledWith("Please enter your email and password");
        expect(loginUser).not.toHaveBeenCalled();
    });
});
