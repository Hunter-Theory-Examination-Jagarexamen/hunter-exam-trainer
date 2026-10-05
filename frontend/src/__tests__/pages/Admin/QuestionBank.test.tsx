import {beforeEach, describe, expect, it, vi} from "vitest";
import {act, render, screen, waitFor} from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import QuestionBank from "../../../pages/Admin/QuestionBank";
import apiClient from "../../../api/apiClient";
import type {Question} from "../../../types/question";

vi.mock("../../../api/apiClient", () => ({default: vi.fn()}));

const subjects = [
    {id: 1, name: "Safety", description: "Safety", questionCount: 101},
    {id: 2, name: "Wildlife", description: "Wildlife", questionCount: 1},
];
const questions: Question[] = Array.from({length: 101}, (_, index) => ({
    id: index + 1, questionText: `Safety question ${index + 1}?`,
    optionA: "Safe answer", optionB: "Second answer", optionC: "Third answer", optionD: "Fourth answer",
    correctAnswer: "Safe answer", explanation: "Keep a safe distance.", subject: subjects[0],
}));

beforeEach(() => {
    vi.resetAllMocks();
    vi.mocked(apiClient).mockImplementation(async endpoint => {
        if (endpoint === "/api/subjects") return subjects;
        if (endpoint === "/api/questions?subjectId=1") return questions;
        if (endpoint === "/api/questions?subjectId=2") return [{...questions[0], id: 102, questionText: "Identify wildlife?"}];
        throw new Error(`Unexpected endpoint: ${endpoint}`);
    });
});

describe("Admin question bank", () => {
    it("loads questions, displays read-only details and paginates a bank of 100+ questions", async () => {
        render(<QuestionBank />);
        expect(screen.getByText("Loading subjects...")).toBeInTheDocument();
        expect(await screen.findByRole("heading", {name: "#1: Safety question 1?"})).toBeInTheDocument();
        expect(screen.getAllByRole("article")).toHaveLength(20);
        expect(screen.getAllByText("Correct answer:")).toHaveLength(20);
        expect(screen.getAllByText("Keep a safe distance.", {exact: false})).toHaveLength(20);
        expect(screen.getByRole("button", {name: "Previous"})).toBeDisabled();
        await userEvent.click(screen.getByRole("button", {name: "Next"}));
        expect(screen.getByRole("heading", {name: "#21: Safety question 21?"})).toBeInTheDocument();
        expect(screen.getByText("Page 2 of 6")).toBeInTheDocument();
    });

    it("searches by question text and ID, resets pagination, and switches subjects", async () => {
        render(<QuestionBank />);
        const input = await screen.findByRole("searchbox");
        await userEvent.click(screen.getByRole("button", {name: "Next"}));
        await userEvent.type(input, "101");
        expect(screen.getByRole("heading", {name: "#101: Safety question 101?"})).toBeInTheDocument();
        expect(screen.getAllByRole("article")).toHaveLength(1);
        await userEvent.clear(input);
        await userEvent.type(input, "no match");
        expect(screen.getByText("No questions match your search.")).toBeInTheDocument();
        await userEvent.clear(input);
        await userEvent.type(input, "SAFETY question 99");
        expect(screen.getByRole("heading", {name: "#99: Safety question 99?"})).toBeInTheDocument();
        await userEvent.selectOptions(screen.getByRole("combobox", {name: "Subject"}), "2");
        expect(await screen.findByRole("heading", {name: "#102: Identify wildlife?"})).toBeInTheDocument();
        expect(screen.getByRole("searchbox")).toHaveValue("");
    });

    it("shows loading questions and ignores responses from a previous subject", async () => {
        let resolveQuestions!: (value: Question[]) => void;
        vi.mocked(apiClient).mockImplementation(async endpoint => {
            if (endpoint === "/api/subjects") return subjects;
            if (endpoint.endsWith("=1")) return new Promise<Question[]>(resolve => { resolveQuestions = resolve; });
            return [{...questions[0], questionText: "Wildlife question?"}];
        });
        render(<QuestionBank />);
        expect(await screen.findByText("Loading questions...")).toBeInTheDocument();
        await userEvent.selectOptions(screen.getByRole("combobox"), "2");
        await screen.findByRole("heading", {name: "#1: Wildlife question?"});
        await act(async () => resolveQuestions(questions));
        expect(screen.queryByText("#1: Safety question 1?")).not.toBeInTheDocument();
    });

    it.each(["subjects", "questions"])("shows an error when %s fail to load", async resource => {
        vi.mocked(apiClient).mockImplementation(async endpoint => {
            if (resource === "questions" && endpoint === "/api/subjects") return subjects;
            throw new Error("Server down");
        });
        render(<QuestionBank />);
        expect(await screen.findByRole("alert")).toHaveTextContent(`Failed to load ${resource}. Please try again.`);
    });

    it.each(["subjects", "questions"])("shows an empty state for no %s", async resource => {
        vi.mocked(apiClient).mockImplementation(async endpoint =>
            resource === "questions" && endpoint === "/api/subjects" ? subjects : []);
        render(<QuestionBank />);
        await waitFor(() => expect(screen.getByText(resource === "subjects"
            ? "No subjects available." : "No questions available for this subject.")).toBeInTheDocument());
    });
});
