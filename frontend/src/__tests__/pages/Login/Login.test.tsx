// @vitest-environment jsdom
import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import Login from "../../../pages/Login/Login";

// Replace the real form with a stub — this test only checks the notice
// wiring, not the form itself.
vi.mock("../../../components/auth/LoginForm", () => ({
    default: ({ notice }: { notice?: string | null }) =>
        <div data-testid="login-form">{notice ?? "no-notice"}</div>,
}));

vi.mock("react-i18next", () => ({
    useTranslation: () => ({ t: (key: string) => key }),
}));

const renderLogin = (search: string) =>
    render(
        <MemoryRouter initialEntries={[`/login${search}`]}>
            <Login />
        </MemoryRouter>,
    );

describe("Login page notices", () => {
    beforeEach(() => vi.clearAllMocks());

    it("shows the account-deleted banner when ?deleted=1", () => {
        renderLogin("?deleted=1");
        expect(screen.getByTestId("login-form")).toHaveTextContent(
            "auth.accountDeletedSuccess",
        );
    });

    it("shows the expired banner when ?expired=1", () => {
        renderLogin("?expired=1");
        expect(screen.getByTestId("login-form")).toHaveTextContent(
            "auth.sessionExpired",
        );
    });

    it("shows no banner with no query params", () => {
        renderLogin("");
        expect(screen.getByTestId("login-form")).toHaveTextContent("no-notice");
    });
});
