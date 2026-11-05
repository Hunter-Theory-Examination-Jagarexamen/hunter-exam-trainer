// Tests for ForgotPassword and ResetPassword pages (#91)

import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter, Routes, Route } from "react-router-dom";
import ForgotPassword from "../../../pages/ForgotPassword/ForgotPassword";
import ResetPassword from "../../../pages/ResetPassword/ResetPassword";
import { forgotPassword, resetPassword } from "../../../services/authService";

vi.mock("../../../services/authService", () => ({
    forgotPassword: vi.fn(),
    resetPassword: vi.fn(),
}));

const VALID_TOKEN = "1234567890123456789012345678901234567890123";

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

function renderResetPassword(hashToken = `#token=${VALID_TOKEN}`) {
    Object.defineProperty(window, "location", {
        writable: true,
        configurable: true,
        value: new URL(`http://localhost/reset-password${hashToken}`),
    });

    render (
        <MemoryRouter initialEntries={["/reset-password"]}>
            <Routes>
                <Route path="/reset-password" element={<ResetPassword />} />
                <Route path="/login" element={<p>Login page</p>} />
            </Routes>
        </MemoryRouter>
    );
}

describe("Password Recovery Pages", () => {
    beforeEach(() => {
        vi.clearAllMocks();
        Object.defineProperty(window, "location", {
            writable: true,
            configurable: true,
            value: new URL("http://localhost/"),
        });
        vi.spyOn(console, "error").mockImplementation(() => {});
});

    afterEach(() => {
    vi.restoreAllMocks();
    });

describe("ForgotPassword Page", () => {
    it("sends request link on valid email submission", async () => {
        vi.mocked(forgotPassword).mockResolvedValueOnce({message: "Reset link sent"});
        renderForgotPassword();

        const emailInput = screen.getByLabelText(/email/i);
        const submitButton = screen.getByRole("button", {name: /send/i});

        await userEvent.type(emailInput, "test@example.com");
        await userEvent.click(submitButton);

        expect(forgotPassword).toHaveBeenCalledWith("test@example.com");
        expect(await screen.findByText(/reset link sent/i)).toBeInTheDocument();
    });

    it("displays error message when backend request fails", async () => {
        vi.mocked(forgotPassword).mockRejectedValueOnce(new Error("User not found"));
        renderForgotPassword();

        const emailInput = screen.getByLabelText(/email/i);
        const submitButton = screen.getByRole("button", {name: /send/i});

        await userEvent.type(emailInput, "unknown@example.com");
        await userEvent.click(submitButton);

        expect(await screen.findByRole("alert")).toHaveTextContent("User not found");
    });
});

    describe("ResetPassword Page", () => {
        it("shows error when reset token is missing from URL hash", async () => {
            renderResetPassword("");

            expect(await screen.findByText(/reset link is invalid/i)).toBeInTheDocument();
            expect(screen.queryByLabelText(/confirm new password/i)).not.toBeInTheDocument();
        });

        it("resets password successfully with valid input and token", async () => {
            vi.mocked(resetPassword).mockResolvedValueOnce({message: "Password reset successful"});
            renderResetPassword(`#token=${VALID_TOKEN}`);

            const passwordInput = screen.getByLabelText(/^new password/i);
            const confirmInput = screen.getByLabelText(/confirm new password/i);
            const submitButton = screen.getByRole("button", {name: /reset password/i});

            await userEvent.type(passwordInput, "ValidPassword123");
            await userEvent.type(confirmInput, "ValidPassword123");
            await userEvent.click(submitButton);

            expect(resetPassword).toHaveBeenCalledWith(VALID_TOKEN, "ValidPassword123");
            expect(await screen.findByText(/password reset successful/i)).toBeInTheDocument();

        });

        it("shows error when passwords do not match", async () => {
            renderResetPassword(`#token=${VALID_TOKEN}`);

            const passwordInput = screen.getByLabelText(/^new password/i);
            const confirmInput = screen.getByLabelText(/confirm new password/i);
            const submitButton = screen.getByRole("button", {name: /reset password/i});

            await userEvent.type(passwordInput, "ValidPassword123");
            await userEvent.type(confirmInput, "MismatchPassword123");
            await userEvent.click(submitButton);

            expect(await screen.findByRole("alert")).toHaveTextContent(/passwords do not match/i);
            expect(resetPassword).not.toHaveBeenCalled();
        });

        it("shows error when password is under 8 characters", async () => {
            renderResetPassword(`#token=${VALID_TOKEN}`);

            const passwordInput = screen.getByLabelText(/^new password/i);
            const confirmInput = screen.getByLabelText(/confirm new password/i);
            const submitButton = screen.getByRole("button", {name: /reset password/i});

            await userEvent.type(passwordInput, "Short1");
            await userEvent.type(confirmInput, "Short1");
            await userEvent.click(submitButton);

            expect(await screen.findByRole("alert")).toHaveTextContent(/at least 8 characters/i);
            expect(resetPassword).not.toHaveBeenCalled();
        });

    });
});