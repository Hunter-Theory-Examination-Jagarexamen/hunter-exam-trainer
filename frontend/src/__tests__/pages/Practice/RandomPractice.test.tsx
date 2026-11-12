// Tests for src/pages/Practice/RandomPractice.tsx (random practice, #151)
//
// Integration-style: the real page with its real child components
// (PracticeQuestionCard, PracticeNavigation, PracticeRoundResult). Only the
// backend is mocked, so the tests also check which endpoints the page calls.

import { StrictMode } from "react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { act, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { createMemoryRouter, RouterProvider } from "react-router-dom";
import RandomPractice from "../../../pages/Practice/RandomPractice";
import apiClient from "../../../api/apiClient";

vi.mock("../../../api/apiClient", () => ({ default: vi.fn() }));
const apiClientMock = vi.mocked(apiClient);

// Two made-up questions, as POST /api/practice/attempts/random returns them:
// picked by the server from all subjects, without correct answers, and with
// no subject name because the round mixes subjects.
const round = {
    attemptId: 11,
    subjectName: null,
    questions: [
        { id: 1, questionText: "Wildlife question?", optionA: "A1", optionB: "B1", optionC: "C1", optionD: "D1", imageUrl: null },
        { id: 2, questionText: "Legislation question?", optionA: "A2", optionB: "B2", optionC: "C2", optionD: "D2", imageUrl: null },
    ],
};

// The fake backend: answers differently depending on which endpoint is called.
// B is the correct option for every question. Like the real server, it counts
// the answers it got, so finish returns the right totals.
function mockBackend() {
    let answered = 0;
    let correct = 0;
    apiClientMock.mockImplementation(async (endpoint: string, options?: RequestInit) => {
        if (endpoint === "/api/practice/attempts/random") {
            return round;
        }
        if (endpoint === "/api/practice/attempts/11/answers") {
            const { chosenOption } = JSON.parse(options?.body as string);
            answered++;
            if (chosenOption === "B") correct++;
            return { correct: chosenOption === "B", correctOption: "B", explanation: null };
        }
        if (endpoint === "/api/practice/attempts/11/finish") {
            return { correctAnswers: correct, answeredQuestions: answered, totalQuestions: 2 };
        }
        throw new Error(`Unexpected endpoint in test: ${endpoint}`);
    });
}

// A memory "data router" (the page's useBlocker needs one) at /practice/random,
// plus stand-ins for the practice mode cards and another page, so tests can see
// where the user ends up. Returns the router so a test can navigate like the menu does.
function createRouter() {
    return createMemoryRouter([
        { path: "/practice/random", element: <RandomPractice /> },
        { path: "/practice", element: <p>Practice modes</p> },
        { path: "/statistics", element: <p>Statistics page</p> },
    ], { initialEntries: ["/practice/random"] });
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

// Makes the next call to an endpoint fail with an HTTP status, like apiClient's
// ApiError; later calls go to the normal fake backend again.
function failOnce(endpoint: string, status: number) {
    const normal = apiClientMock.getMockImplementation()!;
    let failed = false;
    apiClientMock.mockImplementation(async (url: string, options?: RequestInit) => {
        if (url === endpoint && !failed) {
            failed = true;
            throw Object.assign(new Error(JSON.stringify({ message: "Failed" })), { status });
        }
        return normal(url, options);
    });
}

// All endpoints the page called, in order.
const calledEndpoints = () => apiClientMock.mock.calls.map(([endpoint]) => endpoint);

beforeEach(() => {
    apiClientMock.mockReset();
    mockBackend();
});

describe("RandomPractice", () => {

    it("starts a random round and shows its title, progress and first question", async () => {
        renderPage();

        expect(await screen.findByText("Wildlife question?")).toBeInTheDocument();
        expect(screen.getByRole("heading", { level: 1, name: "Slumpmässig övning" })).toBeInTheDocument();
        expect(screen.getByText("Fråga 1 av 2")).toBeInTheDocument();
        // The round is started by the server endpoint for random practice, with no body.
        expect(apiClientMock).toHaveBeenCalledWith("/api/practice/attempts/random", { method: "POST" });
        expect(calledEndpoints()).not.toContain("/api/practice/attempts");
    });

    it("sends the chosen letter to the server and shows the server's feedback", async () => {
        renderPage();

        await answer("A1");

        expect(apiClientMock).toHaveBeenCalledWith("/api/practice/attempts/11/answers",
            { method: "POST", body: JSON.stringify({ questionId: 1, chosenOption: "A" }) });
        // The correct answer (B1) is only known from the server's reply.
        expect(await screen.findByText(/Fel/)).toHaveTextContent("Rätt svar är: B1");
    });

    it("finishes the round on the server after the last question and shows the result", async () => {
        renderPage();

        await answer("B1");
        await userEvent.click(await screen.findByRole("button", { name: "Nästa" }));
        await answer("A2");
        await userEvent.click(await screen.findByRole("button", { name: "Slutför" }));

        expect(apiClientMock).toHaveBeenCalledWith("/api/practice/attempts/11/finish", { method: "POST" });
        expect(await screen.findByText("Du fick 1 av 2 rätt.")).toBeInTheDocument();

        // Back leads to the practice mode cards, not the subject list.
        await userEvent.click(screen.getByRole("button", { name: "Tillbaka till Quiz" }));
        expect(await screen.findByText("Practice modes")).toBeInTheDocument();
    });

    it("shows a message when saving an answer fails, keeps the choice, and Försök igen sends it again", async () => {
        failOnce("/api/practice/attempts/11/answers", 500);
        renderPage();

        await answer("B1");

        expect(await screen.findByRole("alert")).toHaveTextContent("Det gick inte att spara svaret. Försök igen.");
        // The answer is not lost and the question does not advance on its own.
        expect(screen.getByRole("radio", { name: "B1" })).toBeChecked();
        expect(screen.getByText("Wildlife question?")).toBeInTheDocument();

        await userEvent.click(screen.getByRole("button", { name: "Försök igen" }));

        expect(await screen.findByText("Rätt!")).toBeInTheDocument();
        expect(screen.queryByRole("alert")).not.toBeInTheDocument();
    });

    it("quitting with nothing answered goes back to the practice modes without a result", async () => {
        renderPage();

        await userEvent.click(await screen.findByRole("button", { name: "Avsluta omgången" }));
        expect(screen.getByRole("dialog", { name: "Avsluta omgången?" })).toBeInTheDocument();
        await userEvent.click(screen.getByRole("button", { name: "Avsluta" }));

        expect(await screen.findByText("Practice modes")).toBeInTheDocument();
        expect(apiClientMock).toHaveBeenCalledWith("/api/practice/attempts/11/finish", { method: "POST" });
    });

    it("keeps practising when the quit box is cancelled", async () => {
        renderPage();

        await userEvent.click(await screen.findByRole("button", { name: "Avsluta omgången" }));
        await userEvent.click(screen.getByRole("button", { name: "Fortsätt Quiz" }));

        expect(screen.queryByRole("dialog")).not.toBeInTheDocument();
        expect(screen.getByText("Wildlife question?")).toBeInTheDocument();
        expect(calledEndpoints()).not.toContain("/api/practice/attempts/11/finish");
    });

    it("asks before leaving through the menu, and stays on cancel", async () => {
        const router = renderPage();
        await screen.findByText("Wildlife question?");

        await act(() => router.navigate("/statistics"));

        expect(await screen.findByRole("dialog", { name: "Avsluta omgången?" })).toBeInTheDocument();
        await userEvent.click(screen.getByRole("button", { name: "Fortsätt Quiz" }));

        expect(screen.getByText("Wildlife question?")).toBeInTheDocument();
        expect(router.state.location.pathname).toBe("/practice/random");
        expect(calledEndpoints()).not.toContain("/api/practice/attempts/11/finish");
    });

    it("finishes the round and goes on when leaving is confirmed", async () => {
        const router = renderPage();
        await answer("B1");

        await act(() => router.navigate("/statistics"));
        await userEvent.click(screen.getByRole("button", { name: "Avsluta" }));

        expect(await screen.findByText("Statistics page")).toBeInTheDocument();
        expect(apiClientMock).toHaveBeenCalledWith("/api/practice/attempts/11/finish", { method: "POST" });
    });

    it("starts only one round under StrictMode, which runs effects twice", async () => {
        // Two starts would each close the other's round on the server (#170).
        render(<StrictMode><RouterProvider router={createRouter()} /></StrictMode>);

        expect(await screen.findByText("Wildlife question?")).toBeInTheDocument();
        expect(calledEndpoints().filter((e) => e === "/api/practice/attempts/random")).toHaveLength(1);
    });

    it("shows the loading state while the round is starting", async () => {
        apiClientMock.mockReturnValue(new Promise(() => {}));
        renderPage();

        expect(await screen.findByText("Laddar frågor...")).toBeInTheDocument();
        expect(screen.getByRole("heading", { level: 1, name: "Slumpmässig övning" })).toBeInTheDocument();
    });

    it("shows a message when the question bank is empty", async () => {
        apiClientMock.mockRejectedValue(Object.assign(new Error("No questions"), { status: 409 }));
        renderPage();

        expect(await screen.findByText("Det finns inga frågor att öva på än.")).toBeInTheDocument();
    });

    it("shows a message when the round cannot be loaded", async () => {
        apiClientMock.mockRejectedValue(Object.assign(new Error("Boom"), { status: 500 }));
        renderPage();

        expect(await screen.findByText("Det gick inte att ladda frågorna. Försök igen.")).toBeInTheDocument();
    });
});
