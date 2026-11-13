// Tests for src/pages/NotFound/NotFound.tsx

import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import NotFound from "../../pages/NotFound/NotFound";
import { getTokenStatus } from "../../utils/authToken";

vi.mock("../../utils/authToken", () => ({
    getTokenStatus: vi.fn(),
}));

function renderNotFound() {
    render(
        <MemoryRouter initialEntries={["/does-not-exist"]}>
            <NotFound />
        </MemoryRouter>
    );
}

describe("NotFound", () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it("links to the dashboard when the user is logged in", () => {
        vi.mocked(getTokenStatus).mockReturnValue("valid");

        renderNotFound();

        const link = screen.getByRole("link", { name: "Gå till Hem" });
        expect(link).toHaveAttribute("href", "/dashboard");
    });

    it("links to the login page when the visitor is not logged in", () => {
        vi.mocked(getTokenStatus).mockReturnValue("missing");

        renderNotFound();

        const link = screen.getByRole("link", { name: "Gå till inloggningen" });
        expect(link).toHaveAttribute("href", "/login");
    });
});
