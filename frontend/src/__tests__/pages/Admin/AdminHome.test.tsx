import {beforeEach, describe, expect, it, vi} from "vitest";
import {render, screen} from "@testing-library/react";
import {MemoryRouter} from "react-router-dom";
import AdminHome from "../../../pages/Admin/AdminHome";
import apiClient from "../../../api/apiClient";

vi.mock("../../../api/apiClient", () => ({default: vi.fn()}));

const stats = {questionCount: 42, subjectCount: 5, studentCount: 17};
// What apiClient throws for an HTTP error: an Error with the response body as message, plus the status.
const httpError = (status: number, body: string) => Object.assign(new Error(body), {status});

const renderPage = () => render(<MemoryRouter><AdminHome /></MemoryRouter>);

describe("Admin overview cards", () => {
    beforeEach(() => { vi.resetAllMocks(); });

    it("shows a loading state instead of counts until the request finishes", () => {
        vi.mocked(apiClient).mockReturnValue(new Promise(() => {}));
        renderPage();
        expect(screen.getByRole("status")).toHaveTextContent("Loading admin overview...");
        expect(screen.queryByRole("link", {name: /Questions/})).not.toBeInTheDocument();
        expect(apiClient).toHaveBeenCalledWith("/api/admin/stats");
    });

    it("shows one clickable card per tool with its backend count", async () => {
        vi.mocked(apiClient).mockResolvedValue(stats);
        renderPage();
        const questions = await screen.findByRole("link", {name: /Questions/});
        expect(questions).toHaveAttribute("href", "/admin/questions");
        expect(questions).toHaveTextContent("42");
        const subjects = screen.getByRole("link", {name: /Subjects/});
        expect(subjects).toHaveAttribute("href", "/admin/subjects");
        expect(subjects).toHaveTextContent("5");
        const learners = screen.getByRole("link", {name: /Learners/});
        expect(learners).toHaveAttribute("href", "/admin/learners");
        expect(learners).toHaveTextContent("17");
        expect(apiClient).toHaveBeenCalledWith("/api/admin/stats");
    });

    it("shows genuine zero counts from a successful response", async () => {
        vi.mocked(apiClient).mockResolvedValue({questionCount: 0, subjectCount: 0, studentCount: 0});
        renderPage();
        await screen.findByRole("link", {name: /Questions/});
        expect(screen.getAllByText("0")).toHaveLength(3);
        expect(screen.queryByRole("alert")).not.toBeInTheDocument();
    });

    it("shows an error instead of fabricated zero counts when the request fails", async () => {
        vi.mocked(apiClient).mockRejectedValue(httpError(500, '{"message":"Boom"}'));
        renderPage();
        expect(await screen.findByRole("alert")).toHaveTextContent("Failed to load the admin overview.");
        expect(screen.queryByText("0")).not.toBeInTheDocument();
        expect(screen.queryByRole("link", {name: /Questions/})).not.toBeInTheDocument();
    });

    it("explains a missing permission instead of showing counts", async () => {
        vi.mocked(apiClient).mockRejectedValue(httpError(403, '{"message":"You do not have permission to do this"}'));
        renderPage();
        expect(await screen.findByRole("alert"))
            .toHaveTextContent("You do not have permission to view the admin overview.");
        expect(screen.queryByRole("link", {name: /Questions/})).not.toBeInTheDocument();
    });
});
