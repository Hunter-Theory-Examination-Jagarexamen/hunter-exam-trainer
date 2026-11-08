// Tests for src/pages/ForgotPassword/ForgotPassword.tsx

import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter, Routes, Route } from "react-router-dom";
import ForgotPassword from "../../pages/ForgotPassword/ForgotPassword";
import { forgotPassword } from "../../services/authService.ts";

vi.mock("../../services/authService.ts", () => ({
    forgotPassword: vi.fn(),
}));

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
        renderForgotPassword();
        expect(screen.queryByRole("alert")).not.toBeInTheDocument();
    });

    it("shows an error message when the reset request fails", async () => {
        vi.mocked(forgotPassword).mockRejectedValue(
            new Error("E-post hittades inte")
        );

        renderForgotPassword();

        await userEvent.type(
            screen.getByLabelText("E-post"),
            "nobody@example.com"
        );
        await userEvent.click(
            screen.getByRole("button", { name: "Skicka återställningslänk" })
        );

        await waitFor(() =>
            expect(screen.getByRole("alert")).toHaveTextContent("E-post hittades inte")
        );
    });

    it("shows a success message when the reset request succeeds", async () => {
        vi.mocked(forgotPassword).mockResolvedValue({
            message: "Kontrollera din e-post för en återställningslänk.",
        });

        renderForgotPassword();

        await userEvent.type(
            screen.getByLabelText("E-post"),
            "anna@example.com"
        );
        await userEvent.click(
            screen.getByRole("button", { name: "Skicka återställningslänk" })
        );

        expect(
            await screen.findByText("Kontrollera din e-post för en återställningslänk.")
        ).toBeInTheDocument();

        expect(screen.queryByRole("alert")).not.toBeInTheDocument();
    });
});
