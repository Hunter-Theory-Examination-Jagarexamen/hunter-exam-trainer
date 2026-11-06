// Tests for opening a question from a link: /admin/questions?edit=<id>
// (used by the feedback page to open a reported question).

import {beforeEach, describe, expect, it, vi} from "vitest";
import {render, screen} from "@testing-library/react";
import {MemoryRouter} from "react-router-dom";
import apiClient from "../../../api/apiClient";
import QuestionBank from "../../../pages/Admin/QuestionBank";
import type {Question} from "../../../types/question";

vi.mock("../../../api/apiClient", () => ({default: vi.fn()}));

const subjects = [{id: 1, name: "Safety", description: "Safety", questionCount: 0},
    {id: 3, name: "Wildlife", description: "Wildlife", questionCount: 1}];
const question: Question = {id: 12, questionText: "Vilket djur?", optionA: "Älg", optionB: "Rådjur",
    optionC: "Räv", optionD: "Björn", correctAnswer: "B", explanation: "", subject: subjects[1]};

const renderAt = (url: string) =>
    render(<MemoryRouter initialEntries={[url]}><QuestionBank /></MemoryRouter>);

beforeEach(() => {
    vi.resetAllMocks();
    vi.mocked(apiClient).mockImplementation(async (endpoint: string) => {
        if (endpoint === "/api/subjects") return subjects;
        if (endpoint === "/api/admin/questions/12") return question;
        if (endpoint === "/api/admin/questions?subjectId=1") return [];
        if (endpoint === "/api/admin/questions?subjectId=3") return [question];
        throw Object.assign(new Error('{"message":"Question not found"}'), {status: 404});
    });
});

describe("Question Bank opened from a link", () => {
    it("opens the linked question in the editor, in its own subject", async () => {
        renderAt("/admin/questions?edit=12");

        expect(await screen.findByLabelText("Frågetext (obligatoriskt)", {}, {timeout: 5000}))
            .toHaveValue("Vilket djur?");
        expect(screen.getByLabelText("Ämne", {selector: "#question-subject"})).toHaveValue("3");
    });

    it("says so when the linked question no longer exists, and shows the first subject", async () => {
        renderAt("/admin/questions?edit=99");

        expect(await screen.findByRole("alert")).toHaveTextContent("Fråga 99 finns inte längre.");
        expect(screen.getByLabelText("Ämne", {selector: "#question-subject"})).toHaveValue("1");
        expect(screen.queryByLabelText("Frågetext (obligatoriskt)")).not.toBeInTheDocument();
    });
});
