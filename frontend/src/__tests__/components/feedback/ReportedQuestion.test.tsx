// Tests for src/components/feedback/ReportedQuestion.tsx (the reported question
// shown inside a feedback item on the admin page).

import { beforeEach, describe, expect, it, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import apiClient from "../../../api/apiClient";
import ReportedQuestion from "../../../components/feedback/ReportedQuestion";

vi.mock("../../../api/apiClient", () => ({ default: vi.fn() }));
const apiClientMock = vi.mocked(apiClient);

const question = {
    id: 45, questionText: "Vad karaktäriserar orrhanen?", optionA: "Brun fjäderdräkt",
    optionB: "Svart med vita vingband", optionC: "Grå rygg", optionD: "Röd näbb",
    correctAnswer: "B", explanation: "Orrtuppen är svart.", imageUrl: null,
    subject: { id: 9, name: "Fåglar", description: "" },
};

const renderQuestion = () =>
    render(<MemoryRouter><ReportedQuestion questionId={45} /></MemoryRouter>);

beforeEach(() => vi.resetAllMocks());

describe("ReportedQuestion", () => {
    it("loads and shows the question the first time it is opened, with a link to the question bank", async () => {
        apiClientMock.mockResolvedValue(question);
        renderQuestion();
        expect(apiClientMock).not.toHaveBeenCalled();

        await userEvent.click(screen.getByRole("button", { name: "Visa frågan" }));

        expect(await screen.findByRole("heading", { name: "#45: Vad karaktäriserar orrhanen?" })).toBeInTheDocument();
        expect(apiClientMock).toHaveBeenCalledWith("/api/admin/questions/45");
        expect(screen.getByText("Svart med vita vingband")).toBeInTheDocument();
        expect(screen.getByText("Orrtuppen är svart.")).toBeInTheDocument();
        expect(screen.getByRole("link", { name: "Öppna i frågebanken" }))
            .toHaveAttribute("href", "/admin/questions?question=45");
    });

    it("hides it again and doesn't reload it when reopened", async () => {
        apiClientMock.mockResolvedValue(question);
        renderQuestion();

        await userEvent.click(screen.getByRole("button", { name: "Visa frågan" }));
        await screen.findByRole("heading", { name: /#45/ });
        await userEvent.click(screen.getByRole("button", { name: "Dölj frågan" }));
        expect(screen.queryByRole("heading", { name: /#45/ })).not.toBeInTheDocument();

        await userEvent.click(screen.getByRole("button", { name: "Visa frågan" }));
        expect(screen.getByRole("heading", { name: /#45/ })).toBeInTheDocument();
        expect(apiClientMock).toHaveBeenCalledTimes(1);
    });

    it("says when the question no longer exists, and asks to try again on other errors", async () => {
        apiClientMock.mockImplementation(() => Promise.reject(Object.assign(new Error("gone"), { status: 404 })));
        renderQuestion();
        await userEvent.click(screen.getByRole("button", { name: "Visa frågan" }));
        expect(await screen.findByText("Frågan finns inte längre.")).toBeInTheDocument();
    });

    it("asks to try again on other errors", async () => {
        apiClientMock.mockImplementation(() => Promise.reject(Object.assign(new Error("boom"), { status: 500 })));
        renderQuestion();
        await userEvent.click(screen.getByRole("button", { name: "Visa frågan" }));
        expect(await screen.findByRole("alert")).toHaveTextContent("Det gick inte att ladda frågan. Försök igen.");
    });
});
