// Tests for src/components/auth/RegisterForm.tsx

import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter, Routes, Route } from "react-router-dom";
import RegisterForm from "../../../components/auth/RegisterForm";
import apiClient from "../../../api/apiClient";

// Replace the real apiClient (which calls the backend) with a mock.
// apiClient is a default export, so the mock uses the key "default".
vi.mock("../../../api/apiClient", () => ({
    default: vi.fn(),
}));

// Draw the form inside a test router, with a fake /login page
// so we can see whether a successful sign-up navigates there.
function renderRegisterForm() {
    render(
        <MemoryRouter initialEntries={["/register"]}>
            <Routes>
                <Route path="/register" element={<RegisterForm />} />
                <Route path="/login" element={<p>Login page</p>} />
            </Routes>
        </MemoryRouter>
    );
}

// Fill in the form. Every field is valid by default; a test overrides
// only the value it wants to break, e.g. fillForm({ password: "short" }).
async function fillForm({
    fullName = "Anna Andersson",
    email = "anna@example.com",
    password = "secret123",
    confirmPassword = password,
    acceptTerms = true,
}: {
    fullName?: string;
    email?: string;
    password?: string;
    confirmPassword?: string;
    acceptTerms?: boolean;
} = {}) {
    await userEvent.type(screen.getByLabelText("Full Name"), fullName);
    await userEvent.type(screen.getByLabelText("Email"), email);
    await userEvent.type(screen.getByLabelText("Password"), password);
    await userEvent.type(screen.getByLabelText("Confirm Password"), confirmPassword);
    if (acceptTerms) {
        await userEvent.click(screen.getByRole("checkbox"));
    }
}

async function clickCreateAccount() {
    await userEvent.click(screen.getByRole("button", { name: "Create Account" }));
}

describe("RegisterForm", () => {

    beforeEach(() => {
        vi.clearAllMocks(); // forget calls recorded by earlier tests
    });

    it("shows an error when fields are empty", async () => {
        renderRegisterForm();

        await clickCreateAccount();

        expect(screen.getByText("Please fill in all the fields.")).toBeInTheDocument();
        expect(apiClient).not.toHaveBeenCalled();
    });

    it("shows an error when the passwords do not match", async () => {
        renderRegisterForm();

        await fillForm({ confirmPassword: "different123" });
        await clickCreateAccount();

        expect(screen.getByText("Passwords do not match.")).toBeInTheDocument();
        expect(apiClient).not.toHaveBeenCalled();
    });

    it("shows an error when the password is shorter than 8 characters", async () => {
        renderRegisterForm();

        await fillForm({ password: "short" }); // confirmPassword follows password
        await clickCreateAccount();

        expect(screen.getByText("Password must be at least 8 characters long.")).toBeInTheDocument();
        expect(apiClient).not.toHaveBeenCalled();
    });

    it("shows an error when the terms are not accepted", async () => {
        renderRegisterForm();

        await fillForm({ acceptTerms: false });
        await clickCreateAccount();

        expect(screen.getByText("Please accept the Terms of Use and Privacy Policy.")).toBeInTheDocument();
        expect(apiClient).not.toHaveBeenCalled();
    });

    it("registers and goes to the login page when everything is valid", async () => {
        // Arrange: the fake backend accepts the registration
        vi.mocked(apiClient).mockResolvedValue(null);
        renderRegisterForm();

        // Act
        await fillForm();
        await clickCreateAccount();

        // Assert: the backend got the right data, and the login page shows
        expect(apiClient).toHaveBeenCalledWith("/api/auth/register", {
            method: "POST",
            body: JSON.stringify({
                fullName: "Anna Andersson",
                email: "anna@example.com",
                password: "secret123",
            }),
        });
        expect(await screen.findByText("Login page")).toBeInTheDocument();
    });

    it("shows the backend's error message when registration fails", async () => {
        // Arrange: the fake backend rejects the registration
        vi.mocked(apiClient).mockRejectedValue(new Error("Email is already registered"));
        renderRegisterForm();

        // Act
        await fillForm();
        await clickCreateAccount();

        // Assert: the message is shown, and we stay on the form
        expect(await screen.findByText("Email is already registered")).toBeInTheDocument();
        expect(screen.queryByText("Login page")).not.toBeInTheDocument();
    });
});
