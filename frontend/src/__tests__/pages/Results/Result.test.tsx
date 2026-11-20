// Tests for the feedback part of src/pages/Results/Result.tsx (the mock exam result page).

import { beforeEach, describe, expect, it, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import apiClient from "../../../api/apiClient";
import Result from "../../../pages/Results/Result";

vi.mock("../../../api/apiClient", () => ({ default: vi.fn() }));

const result = { score: 86, correctAnswers: 60, incorrectAnswers: 8, unanswered: 2, totalQuestions: 70 };

const renderResult = (state?: object) =>
    render(<MemoryRouter initialEntries={[{ pathname: "/result", state }]}><Result /></MemoryRouter>);

beforeEach(() => vi.resetAllMocks());

describe("Result page feedback", () => {
    it("lets the student send feedback about the exam, without a question", async () => {
        vi.mocked(apiClient).mockResolvedValue("");
        renderResult(result);

        await userEvent.click(screen.getByRole("button", { name: "Ge feedback om provet" }));
        await userEvent.type(screen.getByLabelText("Ditt meddelande"), "Fråga 12 var otydlig");
        await userEvent.click(screen.getByRole("button", { name: "Skicka" }));

        expect(JSON.parse(vi.mocked(apiClient).mock.lastCall![1]!.body as string))
            .toEqual({ type: "APP", text: "Fråga 12 var otydlig" });
    });

    it("offers no feedback button when there is no result to show", () => {
        renderResult();
        expect(screen.queryByRole("button", { name: "Ge feedback om provet" })).not.toBeInTheDocument();
    });

    it("reports the right question from the bookmarked list", async () => {
        vi.mocked(apiClient).mockResolvedValue("");
        renderResult({
            ...result,
            passed: true,
            bookmarkedQuestions: [
                { id: 11, questionText: "Första frågan", optionA: "A", optionB: "B", optionC: "C", optionD: "D" },
                { id: 22, questionText: "Andra frågan", optionA: "A", optionB: "B", optionC: "C", optionD: "D" },
            ],
        });

        expect(screen.getByText("Frågor du bokmärkte")).toBeInTheDocument();
        await userEvent.click(screen.getAllByRole("button", { name: "Rapportera fel i frågan" })[1]);
        await userEvent.type(screen.getByLabelText("Ditt meddelande"), "Fel svar");
        await userEvent.click(screen.getByRole("button", { name: "Skicka" }));

        expect(JSON.parse(vi.mocked(apiClient).mock.lastCall![1]!.body as string))
            .toEqual({ type: "QUESTION", text: "Fel svar", questionId: 22 });
    });
});
