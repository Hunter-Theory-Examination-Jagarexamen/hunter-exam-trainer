// @vitest-environment jsdom
// Tests for src/pages/Admin/LearnerDetailPage.tsx (one user's progress)

import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import LearnerDetailPage from "../../../pages/Admin/LearnerDetailPage";
import apiClient from "../../../api/apiClient.ts";
import type { LearnerProgress } from "../../../types/learnerProgress.ts";

vi.mock("../../../api/apiClient.ts", () => ({
    default: vi.fn(),
}));

// Mock the translation so it returns the key (e.g. "learnerProgress.detail.deleteButton")
// instead of the Swedish text, so our assertions match.
vi.mock("react-i18next", () => ({
    useTranslation: () => ({ t: (key: string) => key }),
}));

const navigate = vi.fn();
vi.mock("react-router-dom", async () => {
    const actual = await vi.importActual<typeof import("react-router-dom")>("react-router-dom");
    return { ...actual, useNavigate: () => navigate };
});

const baseLearner: Omit<LearnerProgress, "mockExams" | "subjectPerformances" | "lastActivityAt"> = {
    userId: 1,
    fullName: "Test Learner",
    email: "test@learner.com",
    role: "STUDENT",
    createdAt: "2026-01-01T00:00:00",
};

const emptyMockExams = {
    count: 0,
    latestScore: null,
    latestAt: null,
    bestScore: null,
    latestPassed: null,
};

function renderPage() {
    render(
        <MemoryRouter initialEntries={["/admin/learners/1"]}>
            <Routes>
                <Route path="/admin/learners/:userId" element={<LearnerDetailPage />} />
            </Routes>
        </MemoryRouter>
    );
}

describe("LearnerDetailPage", () => {

    beforeEach(() => {
        vi.clearAllMocks();
    });

    afterEach(() => {
        vi.restoreAllMocks();
    });

    // ─── EXISTING TESTS ───

    it("shows a learner with no results as empty (not as an error)", async () => {
        vi.mocked(apiClient).mockResolvedValue({
            ...baseLearner,
            lastActivityAt: null,
            subjectPerformances: [],
            mockExams: emptyMockExams,
        });
        renderPage();
        expect(await screen.findByText("Test Learner")).toBeInTheDocument();
        expect(screen.getByText("learnerProgress.noMockExams")).toBeInTheDocument();
        expect(screen.getByText("learnerProgress.noPracticeResults")).toBeInTheDocument();
        expect(screen.getByText("learnerProgress.never")).toBeInTheDocument();
    });

    it("shows mock exam summary for a learner with only exams", async () => {
        vi.mocked(apiClient).mockResolvedValue({
            ...baseLearner,
            lastActivityAt: "2026-10-15T10:00:00",
            subjectPerformances: [],
            mockExams: { count: 2, latestScore: 89, latestAt: "2026-10-15T10:00:00", bestScore: 92, latestPassed: true },
        });
        renderPage();
        expect(await screen.findByText(/2 learnerProgress.taken/)).toBeInTheDocument();
        expect(screen.getByText(/89%/)).toBeInTheDocument();
        expect(screen.getByText(/92%/)).toBeInTheDocument();
        expect(screen.getByText(/learnerProgress.passed/)).toBeInTheDocument();
        expect(screen.getByText("learnerProgress.noPracticeResults")).toBeInTheDocument();
    });

    it("shows practice subjects for a learner with only practice", async () => {
        vi.mocked(apiClient).mockResolvedValue({
            ...baseLearner,
            lastActivityAt: "2026-10-16T10:00:00",
            subjectPerformances: [{ subjectId: 3, subjectName: "Ekologi", percentage: 80 }],
            mockExams: emptyMockExams,
        });
        renderPage();
        expect(await screen.findByText("Ekologi")).toBeInTheDocument();
        expect(screen.getByText("80%")).toBeInTheDocument();
        expect(screen.getByText("learnerProgress.noMockExams")).toBeInTheDocument();
    });

    it("shows both sections for a learner with practice and exams", async () => {
        vi.mocked(apiClient).mockResolvedValue({
            ...baseLearner,
            lastActivityAt: "2026-10-15T10:00:00",
            subjectPerformances: [{ subjectId: 3, subjectName: "Ekologi", percentage: 70 }],
            mockExams: { count: 1, latestScore: 89, latestAt: "2026-10-15T10:00:00", bestScore: 89, latestPassed: true },
        });
        renderPage();
        expect(await screen.findByText(/1 learnerProgress.taken/)).toBeInTheDocument();
        expect(screen.getByText(/learnerProgress.passed/)).toBeInTheDocument();
        expect(screen.getByText("Ekologi")).toBeInTheDocument();
        expect(screen.queryByText("learnerProgress.noMockExams")).not.toBeInTheDocument();
        expect(screen.queryByText("learnerProgress.noPracticeResults")).not.toBeInTheDocument();
    });

    it("shows 'Not passed' when the latest exam failed", async () => {
        vi.mocked(apiClient).mockResolvedValue({
            ...baseLearner,
            lastActivityAt: "2026-10-15T10:00:00",
            subjectPerformances: [],
            mockExams: { count: 1, latestScore: 84, latestAt: "2026-10-15T10:00:00", bestScore: 84, latestPassed: false },
        });
        renderPage();
        expect(await screen.findByText(/learnerProgress.notPassed/)).toBeInTheDocument();
    });

    it("asks for the user from the address and shows their name and email", async () => {
        vi.mocked(apiClient).mockResolvedValue({
            ...baseLearner, lastActivityAt: null, subjectPerformances: [], mockExams: emptyMockExams,
        });
        renderPage();
        expect(await screen.findByRole("heading", { name: "Test Learner" })).toBeInTheDocument();
        expect(screen.getByText("test@learner.com")).toBeInTheDocument();
        expect(apiClient).toHaveBeenCalledWith("/api/admin/learners/1");
        expect(screen.getByRole("link", { name: "learnerProgress.backToList" })).toHaveAttribute("href", "/admin/learners");
    });

    it("says when the user doesn't exist", async () => {
        vi.mocked(apiClient).mockImplementation(() =>
            Promise.reject(Object.assign(new Error("not found"), { status: 404 })));
        renderPage();
        expect(await screen.findByRole("alert")).toHaveTextContent("learnerProgress.notFound");
    });

    // ─── NEW TESTS FOR #194 (Admin Delete Student) ───

    it("shows the delete button for a student", async () => {
        vi.mocked(apiClient).mockResolvedValue({
            ...baseLearner, lastActivityAt: null, subjectPerformances: [], mockExams: emptyMockExams,
        });
        renderPage();

        expect(await screen.findByText("learnerProgress.detail.deleteButton")).toBeInTheDocument();
    });

    it("does not show the delete button for an admin", async () => {
        vi.mocked(apiClient).mockResolvedValue({
            ...baseLearner,
            role: "ADMIN",
            lastActivityAt: null, subjectPerformances: [], mockExams: emptyMockExams,
        });
        renderPage();

        await screen.findByText("Test Learner");
        expect(screen.queryByText("learnerProgress.detail.deleteButton")).not.toBeInTheDocument();
    });

    it("opens the confirmation dialog with the student's name", async () => {
        vi.mocked(apiClient).mockResolvedValue({
            ...baseLearner, lastActivityAt: null, subjectPerformances: [], mockExams: emptyMockExams,
        });
        renderPage();

        fireEvent.click(await screen.findByText("learnerProgress.detail.deleteButton"));
        expect(screen.getByText("learnerProgress.detail.deleteConfirmTitle")).toBeInTheDocument();
    });

    it("cancel closes the dialog without calling the API", async () => {
        vi.mocked(apiClient).mockResolvedValue({
            ...baseLearner, lastActivityAt: null, subjectPerformances: [], mockExams: emptyMockExams,
        });
        renderPage();

        fireEvent.click(await screen.findByText("learnerProgress.detail.deleteButton"));
        fireEvent.click(screen.getByText("learnerProgress.detail.deleteConfirmCancel"));

        expect(screen.queryByText("learnerProgress.detail.deleteConfirmTitle")).not.toBeInTheDocument();
        expect(apiClient).not.toHaveBeenCalledWith("/api/admin/users/1", { method: "DELETE" });
    });

    it("confirm calls deleteStudent and navigates to the list", async () => {
        vi.mocked(apiClient).mockResolvedValueOnce({
            ...baseLearner, lastActivityAt: null, subjectPerformances: [], mockExams: emptyMockExams,
        });

        let resolveDelete: (value: any) => void;
        vi.mocked(apiClient).mockImplementationOnce(
            () => new Promise(resolve => { resolveDelete = resolve; })
        );

        renderPage();

        fireEvent.click(await screen.findByText("learnerProgress.detail.deleteButton"));

        fireEvent.click(screen.getByText("learnerProgress.detail.deleteConfirmButton"));

        resolveDelete!(undefined);

        await waitFor(() => {
            expect(apiClient).toHaveBeenCalledWith("/api/admin/users/1", { method: "DELETE" });
        });
        expect(navigate).toHaveBeenCalledWith(
            "/admin/learners",
            { replace: true, state: { deleted: "Test Learner" } }
        );
    });

    it("shows an error and does not navigate if the API call fails", async () => {
        vi.mocked(apiClient).mockImplementation((url) => {
            if (url === "/api/admin/users/1") {
                return Promise.reject(new Error(JSON.stringify({ message: "Server error" })));
            }
            return Promise.resolve({
                ...baseLearner, lastActivityAt: null, subjectPerformances: [], mockExams: emptyMockExams,
            });
        });

        renderPage();

        fireEvent.click(await screen.findByText("learnerProgress.detail.deleteButton"));

        fireEvent.click(screen.getByText("learnerProgress.detail.deleteConfirmButton"));

        expect(await screen.findByText("Server error")).toBeInTheDocument();
        expect(navigate).not.toHaveBeenCalled();
    });
});
