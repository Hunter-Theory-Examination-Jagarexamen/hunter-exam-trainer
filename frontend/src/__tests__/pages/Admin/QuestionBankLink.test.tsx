// Tests for showing a question from a link: /admin/questions?question=<id>
// (used by the feedback page for a reported question).

import {beforeEach, describe, expect, it, vi} from "vitest";
import {render, screen, waitFor} from "@testing-library/react";
import {MemoryRouter} from "react-router-dom";
import apiClient from "../../../api/apiClient";
import QuestionBank from "../../../pages/Admin/QuestionBank";
import type {Question} from "../../../types/question";

vi.mock("../../../api/apiClient", () => ({default: vi.fn()}));

const subjects = [{id: 1, name: "Safety", description: "Safety", questionCount: 0},
    {id: 3, name: "Wildlife", description: "Wildlife", questionCount: 25}];
// 25 questions in subject 3, so the linked one (#40, the 21st) is on page 2.
const questions: Question[] = Array.from({length: 25}, (_, i) => ({
    id: 20 + i, questionText: `Fråga nummer ${20 + i}?`, optionA: "Älg", optionB: "Rådjur",
    optionC: "Räv", optionD: "Björn", correctAnswer: "B", explanation: "", subject: subjects[1]}));
const linked = questions[20];

const renderAt = (url: string) =>
    render(<MemoryRouter initialEntries={[url]}><QuestionBank /></MemoryRouter>);

const subjectSelect = () => screen.getByLabelText("Ämne", {selector: "#question-subject"});

beforeEach(() => {
    vi.resetAllMocks();
    vi.mocked(apiClient).mockImplementation(async (endpoint: string) => {
        if (endpoint === "/api/subjects") return subjects;
        if (endpoint === "/api/admin/questions/40") return linked;
        if (endpoint === "/api/admin/questions/50") throw Object.assign(new Error("boom"), {status: 500});
        if (endpoint === "/api/admin/questions?subjectId=1") return [];
        if (endpoint === "/api/admin/questions?subjectId=3") return questions;
        throw Object.assign(new Error('{"message":"Question not found"}'), {status: 404});
    });
});

describe("Question Bank opened from a link", () => {
    it("shows the linked question highlighted, on its page, without opening the editor", async () => {
        renderAt("/admin/questions?question=40");

        const card = (await screen.findByRole("heading", {name: "#40: Fråga nummer 40?"}, {timeout: 5000}))
            .closest("article")!;
        expect(card).toHaveClass("question-bank-linked");
        // Focus is moved in an effect after the list is drawn, so wait for it.
        await waitFor(() => expect(card).toHaveFocus());
        expect(subjectSelect()).toHaveValue("3");
        expect(screen.getByText("Sida 2 av 2")).toBeInTheDocument();
        expect(screen.queryByLabelText("Frågetext (obligatoriskt)")).not.toBeInTheDocument();
    });

    it("says so when the linked question no longer exists, and shows the first subject", async () => {
        renderAt("/admin/questions?question=99");

        expect(await screen.findByRole("alert")).toHaveTextContent("Fråga 99 finns inte längre.");
        expect(subjectSelect()).toHaveValue("1");
    });

    it("gives a try-again message for other errors", async () => {
        renderAt("/admin/questions?question=50");

        expect(await screen.findByRole("alert")).toHaveTextContent("Det gick inte att öppna fråga 50. Försök igen.");
    });
});
