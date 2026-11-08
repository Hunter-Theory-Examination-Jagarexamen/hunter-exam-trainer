// Tests for src/pages/Admin/LearnerProgressPage.tsx (/admin/learners: the list of names).

import { beforeEach, describe, expect, it, vi } from "vitest";
import { render, screen, within } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import apiClient from "../../../api/apiClient";
import LearnerProgressPage from "../../../pages/Admin/LearnerProgressPage";

vi.mock("../../../api/apiClient", () => ({ default: vi.fn() }));

// As the backend sends it: admins first, then students, each by name.
const people = [
    { userId: 5, fullName: "Zara Admin", role: "ADMIN" },
    { userId: 2, fullName: "Anna Andersson", role: "STUDENT" },
    { userId: 7, fullName: "Bertil Berg", role: "STUDENT" },
];

const renderPage = () => render(<MemoryRouter><LearnerProgressPage /></MemoryRouter>);

beforeEach(() => vi.resetAllMocks());

describe("LearnerProgressPage (list)", () => {
    it("shows admins and students in labelled groups, each name linking to its detail page", async () => {
        vi.mocked(apiClient).mockResolvedValue(people);
        renderPage();

        const admins = await screen.findByRole("region", { name: "Administratörer (1)" });
        const students = screen.getByRole("region", { name: "Studenter (2)" });
        expect(within(admins).getByRole("link", { name: "Zara Admin" })).toHaveAttribute("href", "/admin/learners/5");
        expect(within(students).getAllByRole("link").map(link => link.textContent))
            .toEqual(["Anna Andersson", "Bertil Berg"]);
        expect(within(students).getByRole("link", { name: "Bertil Berg" })).toHaveAttribute("href", "/admin/learners/7");
        expect(apiClient).toHaveBeenCalledWith("/api/admin/learners");
    });

    it("says when there are no students yet", async () => {
        vi.mocked(apiClient).mockResolvedValue([people[0]]);
        renderPage();

        expect(await screen.findByText("Inga studenter ännu.")).toBeInTheDocument();
        expect(screen.queryByRole("region", { name: /Studenter/ })).not.toBeInTheDocument();
    });

    it("shows an error when the list can't be loaded", async () => {
        vi.mocked(apiClient).mockImplementation(() => Promise.reject(Object.assign(new Error("boom"), { status: 500 })));
        renderPage();

        expect(await screen.findByRole("alert")).toHaveTextContent("Kunde inte ladda studenternas framsteg. Försök igen.");
    });
});
