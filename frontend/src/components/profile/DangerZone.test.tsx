import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import DangerZone from "../../components/profile/DangerZone";
import * as userApi from "../../api/userApi";
import { ApiError } from "../../api/apiClient";

vi.mock("../../api/userApi"); // Updated path

const navigate = vi.fn();
vi.mock("react-router-dom", async () => {
    const actual = await vi.importActual<typeof import("react-router-dom")>("react-router-dom");
    return { ...actual, useNavigate: () => navigate };
});

vi.mock("react-i18next", () => ({
    useTranslation: () => ({ t: (key: string) => key }),
}));

const renderIt = () =>
    render(
        <MemoryRouter>
            <DangerZone />
        </MemoryRouter>,
    );

describe("DangerZone", () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it("reveals the password field only after clicking the delete button", () => {
        renderIt();
        expect(screen.queryByLabelText("profile.currentPasswordLabel")).toBeNull();
        fireEvent.click(screen.getByText("profile.deleteAccountButton"));
        expect(screen.getByLabelText("profile.currentPasswordLabel")).toBeInTheDocument();
    });

    it("wrong password shows the server message and does not log out", async () => {
        vi.mocked(userApi.deleteMyAccount).mockRejectedValueOnce(
            new ApiError(JSON.stringify({ message: "Wrong password" }), 400),
        );
        renderIt();

        fireEvent.click(screen.getByText("profile.deleteAccountButton"));
        fireEvent.change(screen.getByLabelText("profile.currentPasswordLabel"), {
            target: { value: "bad" },
        });
        fireEvent.click(screen.getByText("profile.deleteAccountConfirm"));

        await waitFor(() =>
            expect(screen.getByText("Wrong password")).toBeInTheDocument(),
        );
        expect(navigate).not.toHaveBeenCalled();
    });

    it("correct password navigates to logout?deleted=1", async () => {
        vi.mocked(userApi.deleteMyAccount).mockResolvedValueOnce(undefined);
        renderIt();

        fireEvent.click(screen.getByText("profile.deleteAccountButton"));
        fireEvent.change(screen.getByLabelText("profile.currentPasswordLabel"), {
            target: { value: "secret123" },
        });
        fireEvent.click(screen.getByText("profile.deleteAccountConfirm"));

        await waitFor(() => expect(navigate).toHaveBeenCalledWith("/logout?deleted=1", { replace: true }));
    });

    it("cancel clears the form without calling the API", () => {
        renderIt();
        fireEvent.click(screen.getByText("profile.deleteAccountButton"));
        fireEvent.change(screen.getByLabelText("profile.currentPasswordLabel"), {
            target: { value: "typing" },
        });
        fireEvent.click(screen.getByText("common.cancel"));
        expect(userApi.deleteMyAccount).not.toHaveBeenCalled();
        expect(screen.queryByLabelText("profile.currentPasswordLabel")).toBeNull();
    });
});
