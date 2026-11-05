import {beforeEach, describe, expect, it, vi} from "vitest";
import {fireEvent, render, screen} from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import QuestionBank from "../../../pages/Admin/QuestionBank";
import apiClient from "../../../api/apiClient";
import type {Question} from "../../../types/question";

vi.mock("../../../api/apiClient", () => ({default: vi.fn()}));

const subjects = [
    {id: 1, name: "Safety", description: "Safety", questionCount: 1},
    {id: 2, name: "Wildlife", description: "Wildlife", questionCount: 0}
];

const question: Question = {
    id: 1,
    questionText: "Original question?",
    optionA: "Moose",
    optionB: "Deer",
    optionC: "Fox",
    optionD: "Bear",
    correctAnswer: "C",
    explanation: "An explanation",
    imageUrl: "https://example.com/existing.jpg",
    subject: subjects[0]
};

beforeEach(() => {
    vi.resetAllMocks();
});

describe("Admin question CRUD form", () => {
    it("renders create form correctly and validates required text", async () => {
        render(<QuestionBank subjectId={1} subjects={subjects} onSaved={vi.fn()} onCancel={vi.fn()} />);

        expect(screen.getByRole("heading", {name: "Lägg till fråga"})).toBeInTheDocument();

        await userEvent.click(screen.getByRole("button", {name: "Spara fråga"}));
        expect(screen.getByRole("alert")).toHaveTextContent("Ange frågetext.");
    });

    it("allows filling out and submitting a new question", async () => {
        const onSaved = vi.fn();
        vi.mocked(apiClient).mockResolvedValueOnce({...question, id: 2});

        render(<QuestionBank subjectId={1} subjects={subjects} onSaved={onSaved} onCancel={vi.fn()} />);

        await userEvent.type(screen.getByLabelText("Frågetext (obligatoriskt)"), "New question?");
        await userEvent.type(screen.getByLabelText("Alternativ A (obligatoriskt)"), "Moose");
        await userEvent.type(screen.getByLabelText("Alternativ B (obligatoriskt)"), "Deer");
        await userEvent.type(screen.getByLabelText("Alternativ C (obligatoriskt)"), "Fox");
        await userEvent.type(screen.getByLabelText("Alternativ D (obligatoriskt)"), "Bear");
        await userEvent.click(screen.getByRole("radio", {name: "Rätt alternativ C"}));

        await userEvent.click(screen.getByRole("button", {name: "Spara fråga"}));

        expect(apiClient).toHaveBeenCalledWith("/api/admin/questions", expect.objectContaining({
            method: "POST"
        }));
    });

    it("prepopulates fields when editing an existing question", () => {
        render(<QuestionBank question={question} subjectId={1} subjects={subjects} onSaved={vi.fn()} onCancel={vi.fn()} />);

        expect(screen.getByRole("heading", {name: /Redigera fråga #1/})).toBeInTheDocument();
        expect(screen.getByLabelText("Frågetext (obligatoriskt)")).toHaveValue("Original question?");
        expect(screen.getByLabelText("Alternativ A (obligatoriskt)")).toHaveValue("Moose");
        expect(screen.getByLabelText("Bild-URL (valfritt)")).toHaveValue(question.imageUrl);
    });

    it("handles cancel action correctly", async () => {
        const onCancel = vi.fn();
        render(<QuestionBank subjectId={1} subjects={subjects} onSaved={vi.fn()} onCancel={onCancel} />);

        await userEvent.click(screen.getByRole("button", {name: "Avbryt"}));
        expect(onCancel).toHaveBeenCalled();
    });

    it("validates image URL input length and format", async () => {
        render(<QuestionBank question={question} subjectId={1} subjects={subjects} onSaved={vi.fn()} onCancel={vi.fn()} />);

        const input = screen.getByLabelText("Bild-URL (valfritt)");
        fireEvent.change(input, {target: {value: "invalid-url"}});
        await userEvent.click(screen.getByRole("button", {name: "Spara fråga"}));

        expect(screen.getByRole("alert")).toHaveTextContent("Ange en fullständig HTTP- eller HTTPS-bild-URL.");
    });
});
