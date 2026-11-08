// Tests for src/pages/Exam/Exam.tsx (the mock exam page)
//
// Integration-style: the real page with its real child components
// (ExamHeader, QuestionPanel, ExamNavigation). Only the backend is mocked.

import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import { StrictMode } from "react";
import { render, screen, act, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { createMemoryRouter, RouterProvider } from "react-router-dom";
import Exam from "../../../pages/Exam/Exam";
import apiClient from "../../../api/apiClient.ts";
import type { ExamQuestion } from "../../../types/examQuestion.ts";

vi.mock("../../../api/apiClient.ts", () => ({
    default: vi.fn(),
    ApiError: class ApiError extends Error {
        status: number;
        constructor(message: string, status: number) {
            super(message);
            this.status = status;
        }
    },

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
            return { attemptId: "attempt-1", questions };
        }
        if (endpoint.match(/^\/api\/exam\/[^/]+\/answer$/)) {
            return null;  // 204
        }
        if (endpoint.match(/^\/api\/exam\/[^/]+\/abandon$/)) {
            return null;  // 204
        }
        if (endpoint === "/api/exam/submit") {
            return { totalQuestions: 2, correctAnswers: 1, incorrectAnswers: 1, unanswered: 0, score: 50 };
        }
        throw new Error(`Unexpected endpoint: ${endpoint}`);
    });
}

// A memory "data router" (the page's useBlocker needs one) at /exam, plus
// stand-ins for the result page and other menu destinations, so tests can see
// where the user ends up. Returns the router so a test can navigate like the
// menu or the browser Back button does.
function createExamRouter(initialEntries: string[] = ["/exam"]) {
    return createMemoryRouter([
        { path: "/exam", element: <Exam /> },
        { path: "/result", element: <p>Result page</p> },
        { path: "/statistics", element: <p>Statistics page</p> },
        { path: "/home", element: <p>Home page</p> },
    ], { initialEntries });
}

function renderExam(initialEntries?: string[]) {
    const router = createExamRouter(initialEntries);
    render(<RouterProvider router={router} />);
    return router;
}

// Makes the next call to an endpoint fail with an HTTP status, like apiClient's
// ApiError; later calls go to the normal fake backend again.
function failOnce(endpoint: string, status: number) {
    const normal = vi.mocked(apiClient).getMockImplementation()!;
    let failed = false;
    vi.mocked(apiClient).mockImplementation(async (url: string, options?: RequestInit) => {
        if (url === endpoint && !failed) {
            failed = true;
            throw Object.assign(new Error(JSON.stringify({ message: "Failed" })), { status });
        }
        return normal(url, options);
    });
}

// All endpoints the page called, in order.
const calledEndpoints = () => vi.mocked(apiClient).mock.calls.map(([endpoint]) => endpoint);

describe("Exam page", () => {

    beforeEach(() => {
        vi.clearAllMocks();
        vi.spyOn(console, "log").mockImplementation(() => {});   // Exam logs the result
        vi.spyOn(console, "error").mockImplementation(() => {}); // and logs errors
    });

    afterEach(() => {
        vi.restoreAllMocks();
        // Switch back to the real clock even if a timer test failed halfway.
        vi.useRealTimers();
    });

    it("shows a loading message, then the first question", async () => {
        mockBackend();
        renderExam();

        expect(screen.getByText("Laddar provfrågor...")).toBeInTheDocument();
        expect(await screen.findByText("First question?")).toBeInTheDocument();
        expect(apiClient).toHaveBeenCalledWith("/api/exam/start", { method: "POST" });
    });

    it("starts only one exam under StrictMode, which runs effects twice", async () => {
        // Two starts would both try to create the same totals rows on the server (#174).
        mockBackend();
        render(
            <StrictMode>
                <RouterProvider router={createExamRouter()} />
            </StrictMode>
        );

        expect(await screen.findByText("First question?")).toBeInTheDocument();
        const starts = vi.mocked(apiClient).mock.calls.filter(([endpoint]) => endpoint === "/api/exam/start");
        expect(starts).toHaveLength(1);
    });

    it("saves each answer to the server as it is given", async () => {
        mockBackend();
        renderExam();

        await screen.findByText("First question?");
        await userEvent.click(screen.getByRole("radio", { name: "B1" }));

        // The answer POST should fire
        await waitFor(() => {
            const answerCall = vi.mocked(apiClient).mock.calls.find(
                ([endpoint]) => endpoint === "/api/exam/attempt-1/answer"
            );
            expect(answerCall).toBeDefined();
            const body = JSON.parse(answerCall![1]!.body as string);
            expect(body).toEqual({ questionId: 1, answer: "B" });
        });
    });

    it("does not save the same answer twice", async () => {
        mockBackend();
        renderExam();

        await screen.findByText("First question?");
        const option = screen.getByRole("radio", { name: "B1" });
        await userEvent.click(option);
        await userEvent.click(option);  // same option again

        await waitFor(() => {
            const answerCalls = vi.mocked(apiClient).mock.calls.filter(
                ([endpoint]) => endpoint === "/api/exam/attempt-1/answer"
            );
            expect(answerCalls).toHaveLength(1);
        });
    });

    it("submits only the attempt id, then shows the result page", async () => {
        mockBackend();
        renderExam();

        // Act: answer both questions
        await userEvent.click(await screen.findByRole("radio", { name: "B1" }));
        await userEvent.click(screen.getByRole("button", { name: "Nästa" }));
        await userEvent.click(screen.getByRole("radio", { name: "C2" }));

        // Click the always-visible Submit button (opens modal)
        await userEvent.click(screen.getByRole("button", { name: "Lämna in provet" }));

        // Confirm in modal — both answered, so the confirm button still says "Submit Exam"
        const confirmButtons = await screen.findAllByRole("button", { name: "Lämna in provet" });
        await userEvent.click(confirmButtons[confirmButtons.length - 1]);

        expect(await screen.findByText("Result page")).toBeInTheDocument();

        const submitCall = vi.mocked(apiClient).mock.calls.find(
            ([endpoint]) => endpoint === "/api/exam/submit"
        );
        expect(submitCall).toBeDefined();

        const body = JSON.parse(submitCall![1]!.body as string);
        expect(body).toEqual({ attemptId: "attempt-1" });
    });

    it("shows a failed submit on the page, not in an alert, and can retry", async () => {
        // #188: was a browser alert(), gone once closed.
        mockBackend();
        failOnce("/api/exam/submit", 500);
        const alertSpy = vi.spyOn(window, "alert").mockImplementation(() => {});
        renderExam();
        await screen.findByText("First question?");

        await userEvent.click(screen.getByRole("button", { name: "Lämna in provet" }));
        await userEvent.click(screen.getByRole("button", { name: "Lämna in ändå" }));

        expect(await screen.findByRole("alert")).toHaveTextContent(
            "Kunde inte lämna in provet. Dina svar är sparade på servern — försök igen."
        );
        expect(alertSpy).not.toHaveBeenCalled();
        expect(screen.getByText("First question?")).toBeInTheDocument();

        // Trying again can succeed now; the message is cleared on the new try.
        await userEvent.click(screen.getByRole("button", { name: "Lämna in provet" }));
        await userEvent.click(screen.getByRole("button", { name: "Lämna in ändå" }));

        expect(await screen.findByText("Result page")).toBeInTheDocument();
        expect(screen.queryByRole("alert")).not.toBeInTheDocument();
    });

    it("shows 'Submitting…' while the hand-in is on its way (#188)", async () => {
        mockBackend();
        const normal = vi.mocked(apiClient).getMockImplementation()!;
        vi.mocked(apiClient).mockImplementation(async (url: string, options?: RequestInit) => {
            if (url === "/api/exam/submit") {
                return new Promise(() => {});   // never answers
            }
            return normal(url, options);
        });
        renderExam();
        await screen.findByText("First question?");

        await userEvent.click(screen.getByRole("button", { name: "Lämna in provet" }));
        await userEvent.click(screen.getByRole("button", { name: "Lämna in ändå" }));

        expect(await screen.findByRole("button", { name: "Lämnar in…" })).toBeDisabled();
    });

    it("shows an error message when the exam cannot be loaded", async () => {
        vi.mocked(apiClient).mockRejectedValue(new Error("Server down"));
        renderExam();

        expect(await screen.findByText("Kunde inte ladda provfrågor. Försök igen.")).toBeInTheDocument();
    });

    it("disables Previous on the first question and enables it on subsequent questions", async () => {
        mockBackend();
        renderExam();

        await screen.findByText("First question?");

        const prevButton = screen.getByRole("button", { name: "Föregående" });
        expect(prevButton).toBeDisabled();

        await userEvent.click(screen.getByRole("button", { name: "Nästa" }));
        expect(screen.getByText("Second question?")).toBeInTheDocument();

        expect(prevButton).not.toBeDisabled();
    });

    it("displays the timer countdown in the header", async () => {
        vi.useFakeTimers({ shouldAdvanceTime: true });
        mockBackend();
        renderExam();

        await screen.findByText("First question?");
        expect(screen.getByText(/60:00/i)).toBeInTheDocument();

        await act(async () => {
            vi.advanceTimersByTime(1000);
        });

        expect(screen.getByText(/59:59/i)).toBeInTheDocument();
    });

    it("automatically submits the exam when time runs out", async () => {
        vi.useFakeTimers({ shouldAdvanceTime: true });

        mockBackend();
        renderExam();

        const optionB = await screen.findByRole("radio", { name: "B1" });
        await userEvent.click(optionB);

        await act(async () => {
            await vi.advanceTimersByTimeAsync(60 * 60 * 1000);
        });

        expect(await screen.findByText("Result page")).toBeInTheDocument();

        const submitCall = vi.mocked(apiClient).mock.calls.find(
            ([endpoint]) => endpoint === "/api/exam/submit"
        );

        expect(submitCall).toBeDefined();

        const body = JSON.parse(submitCall![1]!.body as string);
        expect(body).toEqual({ attemptId: "attempt-1" });
    });

    it("shows a clear message when there are too few questions (409 conflict)", async () => {
        vi.mocked(apiClient).mockRejectedValue({
            status: 409,
            message: "Det finns inte tillräckligt många frågor för att starta provet."
        });
        renderExam();

        expect(await screen.findByText("Det finns inte tillräckligt många frågor för att starta provet.")).toBeInTheDocument();
    });

    it("can submit from the first question, not only the last", async () => {
        mockBackend();
        renderExam();

        await screen.findByText("First question?");

        // Click Submit from question 1
        await userEvent.click(screen.getByRole("button", { name: "Lämna in provet" }));

        // Modal appears with unanswered warning
        expect(screen.getByText(/obesvarade frågor/i)).toBeInTheDocument();
        // Confirm — unanswered > 0 so button says "Submit anyway"
        await userEvent.click(screen.getByRole("button", { name: "Lämna in ändå" }));

        expect(await screen.findByText("Result page")).toBeInTheDocument();

        const submitCall = vi.mocked(apiClient).mock.calls.find(
            ([endpoint]) => endpoint === "/api/exam/submit"
        );
        expect(submitCall).toBeDefined();
    });

    it("shows Submit instead of Next on the last question", async () => {
        mockBackend();
        renderExam();

        await screen.findByText("First question?");
        const nextButton = screen.getByRole("button", { name: "Nästa" });
        expect(nextButton).not.toBeDisabled();

        await userEvent.click(nextButton);    // → Q2
        expect(screen.getByText("Second question?")).toBeInTheDocument();
        // Last question: Submit takes Next's place, and there is only one
        // Submit button (the one below the question is hidden) (#188).
        expect(screen.queryByRole("button", { name: "Nästa" })).not.toBeInTheDocument();
        expect(screen.getAllByRole("button", { name: "Lämna in provet" })).toHaveLength(1);
    });

    it("shows 'Submit anyway' when there are unanswered questions", async () => {
        mockBackend();
        renderExam();

        await screen.findByText("First question?");
        // Don't answer anything, just submit
        await userEvent.click(screen.getByRole("button", { name: "Lämna in provet" }));

        // Modal should show "Submit anyway" (unanswered > 0)
        expect(await screen.findByRole("button", { name: "Lämna in ändå" })).toBeInTheDocument();

        // Clean up — cancel so no real submit fires
        await userEvent.click(screen.getByRole("button", { name: "Gå tillbaka" }));
    });

    // Leaving a running exam: menu links, browser Back, tab close (#186).

    it("asks before leaving through the menu, and keeps answers and the timer on cancel", async () => {
        vi.useFakeTimers({ shouldAdvanceTime: true });
        mockBackend();
        const router = renderExam();

        await userEvent.click(await screen.findByRole("radio", { name: "B1" }));
        await act(async () => {
            vi.advanceTimersByTime(61 * 1000);
        });
        expect(document.querySelector(".timer")).toHaveTextContent(/^5[89]:\d\d$/);

        // Same as clicking a menu link while the exam runs.
        await act(() => router.navigate("/statistics"));

        expect(await screen.findByRole("dialog", { name: "Lämna provet?" })).toBeInTheDocument();
        await userEvent.click(screen.getByRole("button", { name: "Stanna kvar" }));

        expect(router.state.location.pathname).toBe("/exam");
        expect(screen.getByText("First question?")).toBeInTheDocument();
        // The answer survived, the countdown carried on (it did not restart),
        // and no exam was abandoned on the server.
        expect(screen.getByRole("radio", { name: "B1" })).toBeChecked();
        expect(document.querySelector(".timer")).toHaveTextContent(/^5[89]:\d\d$/);
        expect(calledEndpoints().filter((e) => e === "/api/exam/start")).toHaveLength(1);
        expect(calledEndpoints()).not.toContain("/api/exam/attempt-1/abandon");
    });

    it("abandons the exam when leaving through the menu is confirmed", async () => {
        mockBackend();
        const router = renderExam();
        await screen.findByText("First question?");

        await act(() => router.navigate("/statistics"));
        await userEvent.click(await screen.findByRole("button", { name: "Lämna provet" }));

        expect(await screen.findByText("Statistics page")).toBeInTheDocument();
        expect(apiClient).toHaveBeenCalledWith("/api/exam/attempt-1/abandon", { method: "POST" });
    });

    it("asks before the browser Back button, and leaves when confirmed", async () => {
        mockBackend();
        const router = renderExam(["/home", "/exam"]);
        await screen.findByText("First question?");

        await act(() => router.navigate(-1));

        expect(await screen.findByRole("dialog", { name: "Lämna provet?" })).toBeInTheDocument();
        await userEvent.click(screen.getByRole("button", { name: "Lämna provet" }));

        expect(await screen.findByText("Home page")).toBeInTheDocument();
        expect(apiClient).toHaveBeenCalledWith("/api/exam/attempt-1/abandon", { method: "POST" });
    });

    it("shows an error and stays when the exam cannot be ended, and can retry", async () => {
        mockBackend();
        failOnce("/api/exam/attempt-1/abandon", 500);
        const router = renderExam();
        await screen.findByText("First question?");

        await act(() => router.navigate("/statistics"));
        await userEvent.click(await screen.findByRole("button", { name: "Lämna provet" }));

        expect(await screen.findByRole("alert")).toHaveTextContent(
            "Det gick inte att avsluta provet på servern. Du kan stanna kvar, eller avsluta ändå."
        );
        expect(router.state.location.pathname).toBe("/exam");
        expect(screen.getByText("First question?")).toBeInTheDocument();

        // "End anyway" tries the server once more; it succeeds now (#188).
        await userEvent.click(screen.getByRole("button", { name: "Avsluta ändå" }));

        expect(await screen.findByText("Statistics page")).toBeInTheDocument();
        expect(screen.queryByRole("alert")).not.toBeInTheDocument();
        expect(calledEndpoints().filter((e) => e === "/api/exam/attempt-1/abandon")).toHaveLength(2);
    });

    it("leaves anyway when the exam still cannot be ended (server down, #188)", async () => {
        mockBackend();
        const normal = vi.mocked(apiClient).getMockImplementation()!;
        vi.mocked(apiClient).mockImplementation(async (url: string, options?: RequestInit) => {
            if (url === "/api/exam/attempt-1/abandon") {
                throw new Error("Failed to fetch");   // no status: server unreachable
            }
            return normal(url, options);
        });
        const router = renderExam();
        await screen.findByText("First question?");

        await act(() => router.navigate("/statistics"));
        await userEvent.click(await screen.findByRole("button", { name: "Lämna provet" }));
        await userEvent.click(await screen.findByRole("button", { name: "Avsluta ändå" }));

        expect(await screen.findByText("Statistics page")).toBeInTheDocument();
    });

    it("asks again from the start after Stay (#188)", async () => {
        mockBackend();
        failOnce("/api/exam/attempt-1/abandon", 500);
        const router = renderExam();
        await screen.findByText("First question?");

        await act(() => router.navigate("/statistics"));
        await userEvent.click(await screen.findByRole("button", { name: "Lämna provet" }));
        await userEvent.click(await screen.findByRole("button", { name: "Stanna kvar" }));

        await act(() => router.navigate("/statistics"));
        expect(await screen.findByRole("button", { name: "Lämna provet" })).toBeInTheDocument();
        expect(screen.queryByRole("button", { name: "Avsluta ändå" })).not.toBeInTheDocument();
    });

    it("keeps the login when logging out is stopped by the leave dialog (#188)", async () => {
        localStorage.setItem("token", "test-token");
        mockBackend();
        const router = renderExam();
        await screen.findByText("First question?");

        await act(() => router.navigate("/logout"));   // what the menu's Log out does

        expect(await screen.findByRole("dialog", { name: "Lämna provet?" })).toBeInTheDocument();
        expect(localStorage.getItem("token")).toBe("test-token");
        localStorage.clear();
    });

    it("goes on without a prompt when the exam was already ended elsewhere (409)", async () => {
        mockBackend();
        failOnce("/api/exam/attempt-1/abandon", 409);
        const router = renderExam();
        await screen.findByText("First question?");

        await act(() => router.navigate("/statistics"));
        await userEvent.click(await screen.findByRole("button", { name: "Lämna provet" }));

        expect(await screen.findByText("Statistics page")).toBeInTheDocument();
        expect(screen.queryByRole("alert")).not.toBeInTheDocument();
    });

    it("sends at most one abandon request even when leaving is clicked twice", async () => {
        mockBackend();
        // The abandon request never finishes, so the dialog stays open.
        vi.mocked(apiClient).mockImplementation(async (endpoint: string) => {
            if (endpoint === "/api/exam/start") {
                return { attemptId: "attempt-1", questions };
            }
            if (endpoint === "/api/exam/attempt-1/abandon") {
                return new Promise(() => { /* pending forever */ });
            }
            if (endpoint.match(/^\/api\/exam\/[^/]+\/answer$/)) {
                return null;
            }
            throw new Error(`Unexpected endpoint: ${endpoint}`);
        });

        const router = renderExam();
        await screen.findByText("First question?");
        await act(() => router.navigate("/statistics"));

        const confirm = await screen.findByRole("button", { name: "Lämna provet" });
        await userEvent.click(confirm);
        expect(confirm).toBeDisabled();
        await userEvent.click(confirm); // ignored while the first request runs

        expect(calledEndpoints().filter((e) => e === "/api/exam/attempt-1/abandon")).toHaveLength(1);
    });

    it("does not ask the browser before the exam has started", async () => {
        mockBackend();
        renderExam();

        // Still loading: no exam is running, so there is nothing to lose.
        const event = new Event("beforeunload", { cancelable: true });
        window.dispatchEvent(event);
        expect(event.defaultPrevented).toBe(false);

        expect(await screen.findByText("First question?")).toBeInTheDocument();
    });

    it("asks the browser before closing or reloading while the exam runs", async () => {
        mockBackend();
        renderExam();
        await screen.findByText("First question?");

        const event = new Event("beforeunload", { cancelable: true });
        window.dispatchEvent(event);

        expect(event.defaultPrevented).toBe(true);
    });

    it("does not ask the browser or the dialog after the exam was submitted", async () => {
        mockBackend();
        const router = renderExam();
        await screen.findByText("First question?");

        await userEvent.click(screen.getByRole("button", { name: "Lämna in provet" }));
        await userEvent.click(screen.getByRole("button", { name: "Lämna in ändå" }));
        expect(await screen.findByText("Result page")).toBeInTheDocument();

        const event = new Event("beforeunload", { cancelable: true });
        window.dispatchEvent(event);
        expect(event.defaultPrevented).toBe(false);

        await act(() => router.navigate("/statistics"));
        expect(await screen.findByText("Statistics page")).toBeInTheDocument();
    });
    });
