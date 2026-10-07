import {beforeEach, describe, expect, it, vi} from "vitest";
import {act, fireEvent, render, screen, within} from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import QuestionBank from "../../../pages/Admin/QuestionBank";
import apiClient from "../../../api/apiClient";
import type {Question} from "../../../types/question";

vi.mock("../../../api/apiClient", () => ({default: vi.fn()}));
const subjects = [{id: 1, name: "Safety", description: "Safety", questionCount: 1},
    {id: 2, name: "Wildlife", description: "Wildlife", questionCount: 0}];
const question: Question = {id: 1, questionText: "Original question?", optionA: "Moose", optionB: "Deer",
    optionC: "Fox", optionD: "Bear", correctAnswer: "Fox", explanation: "An explanation",
    imageUrl: "https://example.com/existing.jpg", subject: subjects[0]};

beforeEach(() => {
    vi.resetAllMocks();
    vi.mocked(apiClient).mockImplementation(async endpoint => {
        if (endpoint === "/api/subjects") return subjects;
        if (endpoint === "/api/questions?subjectId=1") return [question];
        if (endpoint === "/api/questions?subjectId=2") return [];
        throw new Error("Unexpected request");
    });
});

async function open(mode: "create" | "edit" | "delete") {
    render(<QuestionBank />);
    await screen.findByRole("heading", {name: "#1: Original question?"}, {timeout: 5000});
    await userEvent.click(screen.getByRole("button", {name: mode === "create" ? "Add question" : `${mode === "edit" ? "Edit" : "Delete"} question 1`}));
}

async function fillCreate() {
    await userEvent.type(screen.getByLabelText("Question text (required)"), "New question?");
    for (const [letter, value] of [["A", "Moose"], ["B", "Deer"], ["C", "Fox"], ["D", "Bear"]]) {
        await userEvent.type(screen.getByLabelText(`Option ${letter} (required)`), value);
    }
    await userEvent.click(screen.getByRole("radio", {name: "Correct option C"}));
}

const mutations = () => vi.mocked(apiClient).mock.calls.filter(([, options]) => options?.method);

describe("Admin question CRUD", () => {
    it("can add a question to an empty subject and cancel without sending", async () => {
        render(<QuestionBank />);
        await screen.findByRole("heading", {name: "#1: Original question?"}, {timeout: 5000});
        await userEvent.selectOptions(screen.getByLabelText("Subject"), "2");
        await screen.findByText("No questions available for this subject.");
        await userEvent.click(screen.getByRole("button", {name: "Add question"}));
        expect(screen.getByLabelText("Question subject (required)")).toHaveValue("2");
        await userEvent.click(screen.getByRole("button", {name: "Cancel"}));
        expect(screen.queryByRole("form")).not.toBeInTheDocument();
        expect(screen.getByRole("button", {name: "Add question"})).toHaveFocus();
        expect(mutations()).toHaveLength(0);
    });

    it("preserves a matching search after an edit", async () => {
        render(<QuestionBank />);
        await userEvent.type(await screen.findByRole("searchbox"), "Original");
        await userEvent.click(screen.getByRole("button", {name: "Edit question 1"}));
        vi.mocked(apiClient).mockResolvedValueOnce({...question, explanation: "Updated explanation"});
        await userEvent.click(screen.getByRole("button", {name: "Save question"}));
        expect(await screen.findByText("Question #1 updated in Safety.")).toBeInTheDocument();
        expect(screen.getByRole("searchbox")).toHaveValue("Original");
        expect(screen.getByText(/Updated explanation/)).toBeInTheDocument();
    });

    it("validates required text, all four options, and a correct selection before sending", async () => {
        await open("create");
        await userEvent.type(screen.getByLabelText("Question text (required)"), "   ");
        await userEvent.click(screen.getByRole("button", {name: "Save question"}));
        expect(screen.getByRole("alert")).toHaveTextContent("Enter question text.");
        await userEvent.clear(screen.getByLabelText("Question text (required)"));
        await userEvent.type(screen.getByLabelText("Question text (required)"), "Question?");
        for (const letter of ["A", "B", "C", "D"]) {
            await userEvent.click(screen.getByRole("button", {name: "Save question"}));
            expect(screen.getByRole("alert")).toHaveTextContent(`Enter text for option ${letter}.`);
            await userEvent.type(screen.getByLabelText(`Option ${letter} (required)`), "Answer");
        }
        await userEvent.click(screen.getByRole("button", {name: "Save question"}));
        expect(screen.getByRole("alert")).toHaveTextContent("Select the correct option.");
        expect(mutations()).toHaveLength(0);
    });

    it("creates with the latest selected option text and immediately shows the server question", async () => {
        await open("create");
        await fillCreate();
        await userEvent.clear(screen.getByLabelText("Option C (required)"));
        await userEvent.type(screen.getByLabelText("Option C (required)"), "Red fox");
        vi.mocked(apiClient).mockResolvedValueOnce({...question, id: 2, questionText: "New question?", optionC: "Red fox", correctAnswer: "Red fox"});
        await userEvent.click(screen.getByRole("button", {name: "Save question"}));
        expect(apiClient).toHaveBeenCalledWith("/api/admin/questions", {method: "POST", body: JSON.stringify({
            questionText: "New question?", optionA: "Moose", optionB: "Deer", optionC: "Red fox", optionD: "Bear",
            correctAnswer: "Red fox", explanation: "", subjectId: 1})});
        expect(await screen.findByRole("heading", {name: "#2: New question?"})).toBeInTheDocument();
        expect(screen.getByText("Question #2 created in Safety.")).toBeInTheDocument();
        expect(screen.getByRole("option", {name: "Safety (2)"})).toBeInTheDocument();
    });

    it("prepopulates editing, maps correct text, preserves existing image data and immediately updates", async () => {
        await open("edit");
        expect(screen.getByRole("heading", {name: "Edit question #1: Original question?"})).toBeInTheDocument();
        expect(screen.getByLabelText("Question text (required)")).toHaveValue(question.questionText);
        for (const letter of ["A", "B", "C", "D"] as const) {
            expect(screen.getByLabelText(`Option ${letter} (required)`)).toHaveValue(question[`option${letter}`]);
        }
        expect(screen.getByLabelText("Explanation (optional)")).toHaveValue(question.explanation);
        expect(screen.getByLabelText("Question subject (required)")).toHaveValue("1");
        expect(screen.getByRole("radio", {name: "Correct option C"})).toBeChecked();
        await userEvent.clear(screen.getByLabelText("Option C (required)"));
        await userEvent.type(screen.getByLabelText("Option C (required)"), "Arctic fox");
        await userEvent.clear(screen.getByLabelText("Question text (required)"));
        await userEvent.type(screen.getByLabelText("Question text (required)"), "Updated question?");
        vi.mocked(apiClient).mockResolvedValueOnce({...question, questionText: "Updated question?", optionC: "Arctic fox", correctAnswer: "Arctic fox"});
        await userEvent.click(screen.getByRole("button", {name: "Save question"}));
        expect(apiClient).toHaveBeenCalledWith("/api/admin/questions/1", {method: "PUT", body: JSON.stringify({
            questionText: "Updated question?", optionA: "Moose", optionB: "Deer", optionC: "Arctic fox", optionD: "Bear",
            correctAnswer: "Arctic fox", explanation: question.explanation, subjectId: 1, imageUrl: question.imageUrl})});
        const card = (await screen.findByRole("heading", {name: "#1: Updated question?"})).closest("article")!;
        expect(within(card).getByText(/Correct answer:/).parentElement).toHaveTextContent("Arctic fox");
        expect(screen.queryByRole("heading", {name: "#1: Original question?"})).not.toBeInTheDocument();
    });

    it("moves a question to another subject and keeps browsing consistent", async () => {
        await open("edit");
        await userEvent.selectOptions(screen.getByLabelText("Question subject (required)"), "2");
        vi.mocked(apiClient).mockResolvedValueOnce({...question, subject: subjects[1]})
            .mockResolvedValueOnce([{...question, subject: subjects[1]}]);
        await userEvent.click(screen.getByRole("button", {name: "Save question"}));
        expect(await screen.findByText("Question #1 updated in Wildlife.")).toBeInTheDocument();
        expect(await screen.findByRole("heading", {name: "#1: Original question?"})).toBeInTheDocument();
        expect(screen.getByLabelText("Subject")).toHaveValue("2");
        expect(screen.getByRole("option", {name: "Safety (0)"})).toBeInTheDocument();
        expect(screen.getByRole("option", {name: "Wildlife (1)"})).toBeInTheDocument();
        expect(JSON.parse(mutations()[0][1]!.body as string).subjectId).toBe(2);
    });

    it("requires confirmation, cancels without a request, and deletes immediately on confirmation", async () => {
        await open("delete");
        expect(screen.getByRole("heading", {name: "Delete question #1?"})).toBeInTheDocument();
        expect(screen.getByText("Original question?")).toBeInTheDocument();
        expect(mutations()).toHaveLength(0);
        await userEvent.click(screen.getByRole("button", {name: "Cancel deletion"}));
        expect(screen.queryByRole("button", {name: "Confirm deletion"})).not.toBeInTheDocument();
        expect(mutations()).toHaveLength(0);
        await userEvent.click(screen.getByRole("button", {name: "Delete question 1"}));
        vi.mocked(apiClient).mockResolvedValueOnce(null);
        await userEvent.click(screen.getByRole("button", {name: "Confirm deletion"}));
        expect(apiClient).toHaveBeenCalledWith("/api/admin/questions/1", {method: "DELETE"});
        expect(await screen.findByText("Question #1 deleted.")).toBeInTheDocument();
        expect(screen.queryByRole("article")).not.toBeInTheDocument();
        expect(screen.getByText("No questions available for this subject.")).toBeInTheDocument();
    });

    it.each(["create", "edit", "delete"] as const)("prevents duplicate %s requests and restores retry after failure", async mode => {
        await open(mode);
        if (mode === "create") await fillCreate();
        let reject!: (error: Error) => void;
        vi.mocked(apiClient).mockImplementationOnce(() => new Promise((_resolve, rejectRequest) => { reject = rejectRequest; }));
        const button = screen.getByRole("button", {name: mode === "delete" ? "Confirm deletion" : "Save question"});
        fireEvent.click(button);
        fireEvent.click(button);
        if (mode !== "delete") fireEvent.submit(screen.getByRole("form"));
        expect(mutations()).toHaveLength(1);
        expect(button).toBeDisabled();
        expect(screen.getByLabelText("Subject")).toBeDisabled();
        await act(async () => reject(new Error('{"message":"Database technical details"}')));
        expect(screen.getByRole("alert")).toHaveTextContent("The request failed. Please try again.");
        expect(screen.getByRole("alert")).not.toHaveTextContent("Database technical details");
        expect(button).toBeEnabled();
        expect(screen.getByRole("heading", {name: "#1: Original question?"})).toBeInTheDocument();
        vi.mocked(apiClient).mockResolvedValueOnce(mode === "delete" ? null : {...question, id: mode === "create" ? 2 : 1});
        await userEvent.click(button);
        expect(mutations()).toHaveLength(2);
        expect(screen.queryByRole("alert")).not.toBeInTheDocument();
    });

    it.each([['{"message":"404 NOT_FOUND \\"Question not found\\""}', "This question no longer exists."],
        ['API request failed: 403', "You do not have permission to manage questions."],
        ['{"message":"Subject not found"}', "This subject no longer exists."]])("handles existing backend error %s", async (error, message) => {
        await open("edit");
        vi.mocked(apiClient).mockRejectedValueOnce(new Error(error));
        await userEvent.click(screen.getByRole("button", {name: "Save question"}));
        expect(await screen.findByRole("alert")).toHaveTextContent(message);
        expect(screen.getByLabelText("Question text (required)")).toHaveValue(question.questionText);
    });
});
