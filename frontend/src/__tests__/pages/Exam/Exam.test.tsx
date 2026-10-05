// Tests for src/pages/Exam/Exam.tsx (the mock exam page)
//
// Integration-style: the real page with its real child components
// (ExamHeader, QuestionPanel, ExamNavigation). Only the backend is mocked.

import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter, Routes, Route } from "react-router-dom";
import Exam from "../../../pages/Exam/Exam";
import apiClient from "../../../api/apiClient.ts";
import type { ExamQuestion } from "../../../types/examQuestion.ts";

vi.mock("../../../api/apiClient.ts", () => ({
    default: vi.fn(),
}));

// Two made-up questions, as /api/exam/start would return them.
const questions: ExamQuestion[] = [
    { id: 1, questionText: "First question?", optionA: "A1", optionB: "B1", optionC: "C1", optionD: "D1" },
    { id: 2, questionText: "Second question?", optionA: "A2", optionB: "B2", optionC: "C2", optionD: "D2" },
];

// The fake backend: answers differently depending on which endpoint is called.
function mockBackend() {
    vi.mocked(apiClient).mockImplementation(async (endpoint: string) => {
        if (endpoint === "/api/exam/start") {
            return { sessionId: "session-1", questions };
        }
        if (endpoint === "/api/exam/submit") {
            return { totalQuestions: 2, correctAnswers: 1, incorrectAnswers: 1, unanswered: 0, score: 50 };
        }
        throw new Error(`Unexpected endpoint: ${endpoint}`);
    });
}

// Draw the page inside a test router, with a fake /result page.
function renderExam() {
    render(
        <MemoryRouter initialEntries={["/exam"]}>
            <Routes>
                <Route path="/exam" element={<Exam />} />
                <Route path="/result" element={<p>Result page</p>} />
            </Routes>
        </MemoryRouter>
    );
}

describe("Exam page", () => {

    beforeEach(() => {
        vi.clearAllMocks();
        vi.spyOn(console, "log").mockImplementation(() => {});   // Exam logs the result
        vi.spyOn(console, "error").mockImplementation(() => {}); // and logs errors
    });

    afterEach(() => {
        vi.restoreAllMocks();
    });

    it("shows a loading message, then the first question", async () => {
        mockBackend();
        renderExam();

        expect(screen.getByText("Loading exam questions...")).toBeInTheDocument();
        expect(await screen.findByText("First question?")).toBeInTheDocument();
        expect(apiClient).toHaveBeenCalledWith("/api/exam/start", { method: "POST" });
    });

    it("submits the session id, question ids and answers, then shows the result page", async () => {
        mockBackend();
        renderExam();

        // Act: answer both questions and submit
        await userEvent.click(await screen.findByRole("radio", { name: "B1" }));
        await userEvent.click(screen.getByRole("button", { name: "Next" }));
        await userEvent.click(screen.getByRole("radio", { name: "C2" }));
        await userEvent.click(screen.getByRole("button", { name: "Submit Exam" }));

        // Assert: the result page shows
        expect(await screen.findByText("Result page")).toBeInTheDocument();

        // Find the /submit call among everything the mock recorded (see vi.fn's .mock.calls)
        const submitCall = vi.mocked(apiClient).mock.calls.find(
            ([endpoint]) => endpoint === "/api/exam/submit"
        );
        expect(submitCall).toBeDefined();

        // The body is a JSON string; turn it back into an object to compare
        const body = JSON.parse(submitCall![1]!.body as string);
        expect(body).toEqual({
            sessionId: "session-1",
            questionIds: [1, 2],
            answers: { "1": "B", "2": "C" },
        });
    });

    it("shows an error message when the exam cannot be loaded", async () => {
        vi.mocked(apiClient).mockRejectedValue(new Error("Server down"));
        renderExam();

        expect(await screen.findByText("Failed to load exam questions. Please try again.")).toBeInTheDocument();
    });
});
