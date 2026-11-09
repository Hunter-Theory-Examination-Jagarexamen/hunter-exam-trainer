// Tests for src/components/auth/RegisterForm.tsx

import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter, Routes, Route } from "react-router-dom";
import RegisterForm from "../../../components/auth/RegisterForm";
import apiClient from "../../../api/apiClient";

vi.mock("../../../api/apiClient", () => ({
    default: vi.fn(),
}));

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
    await userEvent.type(screen.getByLabelText("Fullständigt namn"), fullName);
    await userEvent.type(screen.getByLabelText("E-postadress"), email);
    await userEvent.type(screen.getByLabelText("Lösenord"), password);
    await userEvent.type(screen.getByLabelText("Bekräfta lösenord"), confirmPassword);
    if (acceptTerms) {
        await userEvent.click(screen.getByRole("checkbox"));
    }
}

async function clickCreateAccount() {
    await userEvent.click(screen.getByRole("button", { name: "Registrera" }));
}

describe("RegisterForm", () => {

    beforeEach(() => {
        vi.clearAllMocks();
    });

    it("shows an error when fields are empty", async () => {
        renderRegisterForm();

        await clickCreateAccount();

        expect(screen.getByText("Vänligen fyll i alla fält.")).toBeInTheDocument();
        expect(apiClient).not.toHaveBeenCalled();
    });

    it("shows an error when the passwords do not match", async () => {
        renderRegisterForm();

        await fillForm({ confirmPassword: "different123" });
        await clickCreateAccount();

        expect(screen.getByText("Lösenorden matchar inte.")).toBeInTheDocument();
        expect(apiClient).not.toHaveBeenCalled();
    });

    it("shows an error when the password is shorter than 8 characters", async () => {
        renderRegisterForm();

        await fillForm({ password: "short" });
        await clickCreateAccount();

        expect(screen.getByText("Lösenordet är för kort.")).toBeInTheDocument();
        expect(apiClient).not.toHaveBeenCalled();
    });

    it("opens the Terms of Use in a pop-up without ticking the box or losing typed text (#212)", async () => {
        renderRegisterForm();
        await userEvent.type(screen.getByLabelText("Fullständigt namn"), "Anna Andersson");

        await userEvent.click(screen.getByRole("button", { name: "användarvillkoren" }));
        expect(screen.getByRole("dialog", { name: "Användarvillkor" })).toBeInTheDocument();

        await userEvent.click(screen.getByRole("button", { name: "Stäng" }));
        expect(screen.queryByRole("dialog")).not.toBeInTheDocument();
        expect(screen.getByRole("checkbox")).not.toBeChecked();
        expect(screen.getByLabelText("Fullständigt namn")).toHaveValue("Anna Andersson");
    });

    it("opens the Privacy Policy in a pop-up (#212)", async () => {
        renderRegisterForm();

        await userEvent.click(screen.getByRole("button", { name: "integritetspolicyn" }));

        expect(screen.getByRole("dialog", { name: "Integritetspolicy" })).toBeInTheDocument();
    });

    it("shows an error when the terms are not accepted", async () => {
        renderRegisterForm();

        await fillForm({ acceptTerms: false });
        await clickCreateAccount();

        expect(screen.getByText("Du måste godkänna användarvillkoren och integritetspolicyn.")).toBeInTheDocument();
    });

    it("registers and goes to the login page when everything is valid", async () => {
        vi.mocked(apiClient).mockResolvedValue(null);
        renderRegisterForm();

        await fillForm();
        await clickCreateAccount();

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
        vi.mocked(apiClient).mockRejectedValue(new Error("E-postadressen är redan registrerad"));
        renderRegisterForm();

        await fillForm();
        await clickCreateAccount();

        expect(await screen.findByText("Ett oväntat fel uppstod.")).toBeInTheDocument();
        expect(screen.queryByText("Login page")).not.toBeInTheDocument();
    });
});
