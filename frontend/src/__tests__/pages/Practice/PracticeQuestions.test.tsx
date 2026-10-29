// Tests for src/pages/Practice/PracticeQuestions.tsx (practice by subject)
//
// Integration-style: the real page with its real child components
// (PracticeQuestionCard, PracticeNavigation). Only the backend is mocked,
// so the tests also check which endpoints the page calls.

import { StrictMode } from "react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { act, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { createMemoryRouter, RouterProvider } from "react-router-dom";
import PracticeQuestions from "../../../pages/Practice/PracticeQuestions";
import apiClient from "../../../api/apiClient";

vi.mock("../../../api/apiClient", () => ({ default: vi.fn() }));
const apiClientMock = vi.mocked(apiClient);

// Two made-up questions, as POST /api/practice/attempts returns them
// (no correct answers).
const round = {
    attemptId: 10,
    subjectName: "Viltvård",
    questions: [
        { id: 1, questionText: "First question?", optionA: "A1", optionB: "B1", optionC: "C1", optionD: "D1", imageUrl: null },
        { id: 2, questionText: "Second question?", optionA: "A2", optionB: "B2", optionC: "C2", optionD: "D2", imageUrl: null },
    ],
};

// The fake backend: answers differently depending on which endpoint is called.
// B is the correct option for every question. Like the real server, it counts
// the answers it got, so finish returns the right totals.
function mockBackend() {
    let answered = 0;
    let correct = 0;
    apiClientMock.mockImplementation(async (endpoint: string, options?: RequestInit) => {
        if (endpoint === "/api/practice/attempts") {
            return round;
        }
        if (endpoint === "/api/practice/attempts/10/answers") {
            const { chosenOption } = JSON.parse(options?.body as string);
            answered++;
            if (chosenOption === "B") correct++;
            return { correct: chosenOption === "B", correctOption: "B", explanation: null };
        }
        if (endpoint === "/api/practice/attempts/10/finish") {
            return { correctAnswers: correct, answeredQuestions: answered, totalQuestions: 2 };
        }
        throw new Error(`Unexpected endpoint in test: ${endpoint}`);
    });
}

// A memory "data router" (the page's useBlocker needs one) at /practice/subjects/3,
// plus stand-ins for the subject list and another page, so tests can see where
// the user ends up. Returns the router so a test can navigate like the menu does.
function createRouter() {
    return createMemoryRouter([
        { path: "/practice/subjects/:subject", element: <PracticeQuestions /> },
        { path: "/practice/subjects", element: <p>Subject list</p> },
        { path: "/statistics", element: <p>Statistics page</p> },
    ], { initialEntries: ["/practice/subjects/3"] });
}

function renderPage() {
    const router = createRouter();
    render(<RouterProvider router={router} />);
    return router;
}

// Pick an option and check it, as the user would.
async function answer(optionText: string) {
    await userEvent.click(await screen.findByRole("radio", { name: optionText }));
    await userEvent.click(screen.getByRole("button", { name: "Kontrollera svar" }));
}

// All endpoints the page called, in order.
const calledEndpoints = () => apiClientMock.mock.calls.map(([endpoint]) => endpoint);

beforeEach(() => {
    apiClientMock.mockReset();
    mockBackend();
});

describe("PracticeQuestions", () => {

    it("starts a round for the subject and shows its name and first question", async () => {
        renderPage();

        expect(await screen.findByText("First question?")).toBeInTheDocument();
        expect(screen.getByText("Viltvård")).toBeInTheDocument();
        expect(screen.getByText("Fråga 1 av 2")).toBeInTheDocument();
        expect(apiClientMock).toHaveBeenCalledWith("/api/practice/attempts",
            { method: "POST", body: JSON.stringify({ subjectId: 3 }) });
    });

    it("sends the chosen letter to the server and shows the server's feedback", async () => {
        renderPage();

        await answer("A1");

        expect(apiClientMock).toHaveBeenCalledWith("/api/practice/attempts/10/answers",
            { method: "POST", body: JSON.stringify({ questionId: 1, chosenOption: "A" }) });
        // The correct answer (B1) is only known from the server's reply.
        expect(await screen.findByText(/Fel/)).toHaveTextContent("Rätt svar är: B1");
    });

    it("shows an answered question read-only after going back", async () => {
        renderPage();

        await answer("B1");
        await userEvent.click(await screen.findByRole("button", { name: "Nästa" }));
        expect(await screen.findByText("Second question?")).toBeInTheDocument();

        await userEvent.click(screen.getByRole("button", { name: "Föregående" }));

        expect(await screen.findByText("Rätt!")).toBeInTheDocument();
        expect(screen.getByRole("radio", { name: "B1" })).toBeChecked();
        expect(screen.getByRole("radio", { name: "A1" })).toBeDisabled();
        // Still only one answer sent: going back doesn't send it again.
        expect(calledEndpoints().filter((e) => e.endsWith("/answers"))).toHaveLength(1);
    });

    it("finishes the round on the server after the last question and shows the result", async () => {
        renderPage();

        await answer("B1");
        await userEvent.click(await screen.findByRole("button", { name: "Nästa" }));
        await answer("A2");
        await userEvent.click(await screen.findByRole("button", { name: "Slutför" }));

        expect(apiClientMock).toHaveBeenCalledWith("/api/practice/attempts/10/finish", { method: "POST" });
        expect(await screen.findByText("Du fick 1 av 2 rätt.")).toBeInTheDocument();

        await userEvent.click(screen.getByRole("button", { name: "Tillbaka till ämnen" }));
        expect(await screen.findByText("Subject list")).toBeInTheDocument();
    });

    it("quitting with nothing answered goes back to the subject list without a result", async () => {
        renderPage();

        await userEvent.click(await screen.findByRole("button", { name: "Avsluta omgången" }));
        expect(screen.getByRole("dialog", { name: "Avsluta omgången?" })).toBeInTheDocument();
        await userEvent.click(screen.getByRole("button", { name: "Avsluta" }));

        expect(await screen.findByText("Subject list")).toBeInTheDocument();
        expect(apiClientMock).toHaveBeenCalledWith("/api/practice/attempts/10/finish", { method: "POST" });
    });

    it("quitting after some answers shows how many were answered", async () => {
        renderPage();

        await answer("B1");
        await userEvent.click(await screen.findByRole("button", { name: "Avsluta omgången" }));
        await userEvent.click(screen.getByRole("button", { name: "Avsluta" }));

        expect(await screen.findByText("Du svarade på 1 av 2 frågor och fick 1 rätt.")).toBeInTheDocument();
    });

    it("keeps practising when the quit box is cancelled", async () => {
        renderPage();

        await userEvent.click(await screen.findByRole("button", { name: "Avsluta omgången" }));
        await userEvent.click(screen.getByRole("button", { name: "Fortsätt öva" }));

        expect(screen.queryByRole("dialog")).not.toBeInTheDocument();
        expect(screen.getByText("First question?")).toBeInTheDocument();
        expect(calledEndpoints()).not.toContain("/api/practice/attempts/10/finish");
    });

    it("never calls the old question list or results endpoints", async () => {
        renderPage();

        await answer("B1");
        await userEvent.click(await screen.findByRole("button", { name: "Nästa" }));
        await answer("B2");
        await userEvent.click(await screen.findByRole("button", { name: "Slutför" }));
        await userEvent.click(await screen.findByRole("button", { name: "Tillbaka till ämnen" }));
        await screen.findByText("Subject list");

        // /api/questions returned every correct answer; /api/practice/results
        // took the score from the browser. Both are replaced by the rounds.
        expect(calledEndpoints().some((e) => e.startsWith("/api/questions"))).toBe(false);
        expect(calledEndpoints()).not.toContain("/api/practice/results");
    });

    it("starts only one round under StrictMode, which runs effects twice", async () => {
        // Two starts would each close the other's round on the server (#170).
        render(<StrictMode><RouterProvider router={createRouter()} /></StrictMode>);

        expect(await screen.findByText("First question?")).toBeInTheDocument();
        expect(calledEndpoints().filter((e) => e === "/api/practice/attempts")).toHaveLength(1);
    });

    it("asks before leaving through the menu, and stays on cancel", async () => {
        const router = renderPage();
        await screen.findByText("First question?");

        // Same as clicking "Statistics" in the menu.
        await act(() => router.navigate("/statistics"));

        expect(await screen.findByRole("dialog", { name: "Avsluta omgången?" })).toBeInTheDocument();
        await userEvent.click(screen.getByRole("button", { name: "Fortsätt öva" }));

        expect(screen.getByText("First question?")).toBeInTheDocument();
        expect(router.state.location.pathname).toBe("/practice/subjects/3");
        expect(calledEndpoints()).not.toContain("/api/practice/attempts/10/finish");
    });

    it("finishes the round and goes on when leaving is confirmed", async () => {
        const router = renderPage();
        await answer("B1");

        await act(() => router.navigate("/statistics"));
        await userEvent.click(await screen.findByRole("button", { name: "Avsluta" }));

        expect(await screen.findByText("Statistics page")).toBeInTheDocument();
        expect(apiClientMock).toHaveBeenCalledWith("/api/practice/attempts/10/finish", { method: "POST" });
    });

    it("doesn't ask again once the round is finished", async () => {
        const router = renderPage();
        await answer("B1");
        await userEvent.click(await screen.findByRole("button", { name: "Avsluta omgången" }));
        await userEvent.click(screen.getByRole("button", { name: "Avsluta" }));
        await screen.findByText("Resultat");

        await act(() => router.navigate("/statistics"));

        expect(await screen.findByText("Statistics page")).toBeInTheDocument();
    });

    it("shows a message when the subject has no questions", async () => {
        apiClientMock.mockRejectedValue(Object.assign(new Error("No questions"), { status: 409 }));
        renderPage();

        expect(await screen.findByText("Det finns inga frågor i det här ämnet än.")).toBeInTheDocument();
    });
});
