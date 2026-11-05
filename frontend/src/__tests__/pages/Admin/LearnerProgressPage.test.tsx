// Tests for src/pages/Admin/LearnerProgressPage.tsx

import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import LearnerProgressPage from "../../../pages/Admin/LearnerProgressPage";
import apiClient from "../../../api/apiClient.ts";
import type { LearnerProgress } from "../../../types/learnerProgress.ts";

vi.mock("../../../api/apiClient.ts", () => ({
    default: vi.fn(),
}));

const baseLearner: Omit<LearnerProgress, "mockExams" | "subjectPerformances" | "lastActivityAt"> = {
    userId: 1,
    fullName: "Test Learner",
    email: "test@learner.com",
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
        <MemoryRouter>
            <LearnerProgressPage />
        </MemoryRouter>
    );
}

describe("LearnerProgressPage", () => {

    beforeEach(() => {
        vi.clearAllMocks();
    });

    afterEach(() => {
        vi.restoreAllMocks();
    });

    it("shows a learner with no results as empty (not as an error)", async () => {
        vi.mocked(apiClient).mockResolvedValue([
            {
                ...baseLearner,
                lastActivityAt: null,
                subjectPerformances: [],
                mockExams: emptyMockExams,
            },
        ]);

        renderPage();

        expect(await screen.findByText("Test Learner")).toBeInTheDocument();
        expect(screen.getByText("Inga låtsasprov ännu.")).toBeInTheDocument();
        expect(screen.getByText("Inga övningsresultat ännu.")).toBeInTheDocument();
        expect(screen.getByText("Aldrig")).toBeInTheDocument();
    });

    it("shows mock exam summary for a learner with only exams", async () => {
        vi.mocked(apiClient).mockResolvedValue([
            {
                ...baseLearner,
                lastActivityAt: "2026-10-15T10:00:00",
                subjectPerformances: [],
                mockExams: {
                    count: 2,
                    latestScore: 89,
                    latestAt: "2026-10-15T10:00:00",
                    bestScore: 92,
                    latestPassed: true,
                },
            },
        ]);

        renderPage();

        expect(await screen.findByText(/2 genomförda/)).toBeInTheDocument();
        expect(screen.getByText(/89%/)).toBeInTheDocument();     // latest
        expect(screen.getByText(/92%/)).toBeInTheDocument();     // best
        expect(screen.getByText(/Godkänd/)).toBeInTheDocument();
        expect(screen.getByText("Inga övningsresultat ännu.")).toBeInTheDocument();
    });

    it("shows practice subjects for a learner with only practice", async () => {
        vi.mocked(apiClient).mockResolvedValue([
            {
                ...baseLearner,
                lastActivityAt: "2026-10-16T10:00:00",
                subjectPerformances: [
                    { subjectId: 3, subjectName: "Ekologi", percentage: 80 },
                ],
                mockExams: emptyMockExams,
            },
        ]);

        renderPage();

        expect(await screen.findByText("Ekologi")).toBeInTheDocument();
        expect(screen.getByText("80%")).toBeInTheDocument();
        expect(screen.getByText("Inga låtsasprov ännu.")).toBeInTheDocument();
    });

    it("shows both sections for a learner with practice and exams", async () => {
        vi.mocked(apiClient).mockResolvedValue([
            {
                ...baseLearner,
                lastActivityAt: "2026-10-15T10:00:00",
                subjectPerformances: [
                    { subjectId: 3, subjectName: "Ekologi", percentage: 70 },
                ],
                mockExams: {
                    count: 1,
                    latestScore: 89,
                    latestAt: "2026-10-15T10:00:00",
                    bestScore: 89,
                    latestPassed: true,
                },
            },
        ]);

        renderPage();

        expect(await screen.findByText(/1 genomförda/)).toBeInTheDocument();
        expect(screen.getByText(/Godkänd/)).toBeInTheDocument();
        expect(screen.getByText("Ekologi")).toBeInTheDocument();
        expect(screen.queryByText("Inga låtsasprov ännu.")).not.toBeInTheDocument();
        expect(screen.queryByText("Inga övningsresultat ännu.")).not.toBeInTheDocument();
    });

    it("shows 'Not passed' when the latest exam failed", async () => {
        vi.mocked(apiClient).mockResolvedValue([
            {
                ...baseLearner,
                lastActivityAt: "2026-10-15T10:00:00",
                subjectPerformances: [],
                mockExams: {
                    count: 1,
                    latestScore: 84,
                    latestAt: "2026-10-15T10:00:00",
                    bestScore: 84,
                    latestPassed: false,
                },
            },
        ]);

        renderPage();

        expect(await screen.findByText(/Ej godkänd/)).toBeInTheDocument();
    });
});
