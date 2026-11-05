// Tests for src/pages/ForgotPassword/ForgotPassword.tsx

import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter, Routes, Route } from "react-router-dom";
import ForgotPassword from "../../pages/ForgotPassword/ForgotPassword";
import { forgotPassword } from "../../services/authService.ts";

// Replace the real authService (which calls the backend) with mock functions.
vi.mock("../../services/authService.ts", () => ({
    forgotPassword: vi.fn(),
}));

// Draw the page inside a test router. The fake /login page lets us
// see whether "Back to Login" navigates there.
function renderForgotPassword() {
    render(
        <MemoryRouter initialEntries={["/forgot-password"]}>
            <Routes>
                <Route path="/forgot-password" element={<ForgotPassword />} />
                <Route path="/login" element={<p>Login page</p>} />
            </Routes>
        </MemoryRouter>
    );
}

describe("ForgotPassword", () => {

    beforeEach(() => {
        vi.clearAllMocks();
    });

    afterEach(() => {
        vi.restoreAllMocks();
    });

    it("shows no error message on initial load", () => {
        // Regression test for #107: opening the page used to show "ACC".
        renderForgotPassword();

        // No alert role should be present until a real error occurs.
        expect(screen.queryByRole("alert")).not.toBeInTheDocument();
    });

    it("shows an error message when the reset request fails", async () => {
        // The backend rejects the request.
        vi.mocked(forgotPassword).mockRejectedValue(
            new Error("Email not found")
        );

        renderForgotPassword();

        // Submit the form.
        await userEvent.type(
            screen.getByLabelText("Email"),
            "nobody@example.com"
        );
        await userEvent.click(
            screen.getByRole("button", { name: "Send Reset Link" })
        );

        // The error should now appear with role="alert".
        await waitFor(() =>
            expect(screen.getByRole("alert")).toHaveTextContent("Email not found")
        );
    });

    it("shows a success message when the reset request succeeds", async () => {
        vi.mocked(forgotPassword).mockResolvedValue({
            message: "Check your email for a reset link.",
        });

        renderForgotPassword();

        await userEvent.type(
            screen.getByLabelText("Email"),
            "anna@example.com"
        );
        await userEvent.click(
            screen.getByRole("button", { name: "Send Reset Link" })
        );

        expect(
            await screen.findByText("Check your email for a reset link.")
        ).toBeInTheDocument();

        // And no alert role should be present.
        expect(screen.queryByRole("alert")).not.toBeInTheDocument();
    });
});