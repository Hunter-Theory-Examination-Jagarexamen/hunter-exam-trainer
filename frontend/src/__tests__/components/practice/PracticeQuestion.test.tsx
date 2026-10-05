// Tests for src/components/practice/PracticeQuestion.tsx
//
// PracticeQuestion is a "controlled" component: the parent page decides which
// answer is selected and whether feedback is shown, and passes that in as props.
// So each test draws it with the props it wants and checks what the user sees.

import { describe, it, expect, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import PracticeQuestion from "../../../components/practice/PracticeQuestion";
import type { PracticeQuestion as PracticeQuestionType } from "../../../types/practiceQuestions";

// A small made-up question. Option "b" is the correct one.
const question: PracticeQuestionType = {
    id: "q1",
    subjectArea: "Test subject",
    question: "Which animal is a deer?",
    options: [
        { id: "a", answer: "Wild boar", isCorrect: false },
        { id: "b", answer: "Roe deer", isCorrect: true },
        { id: "c", answer: "Fox", isCorrect: false },
    ],
};

// Draw the component. Defaults: nothing selected, no feedback, mock callbacks.
// A test overrides only the props it cares about.
function renderQuestion(props: Partial<React.ComponentProps<typeof PracticeQuestion>> = {}) {
    render(
        <PracticeQuestion
            question={question}
            selectedAnswer={null}
            showFeedback={false}
            onAnswerSelect={vi.fn()}
            onCheckAnswer={vi.fn()}
            {...props}
        />
    );
}

describe("PracticeQuestion", () => {

    it("shows the question and all answer choices", () => {
        renderQuestion();

        expect(screen.getByText("Which animal is a deer?")).toBeInTheDocument();
        expect(screen.getByRole("radio", { name: "Wild boar" })).toBeInTheDocument();
        expect(screen.getByRole("radio", { name: "Roe deer" })).toBeInTheDocument();
        expect(screen.getByRole("radio", { name: "Fox" })).toBeInTheDocument();
    });

    it("calls onAnswerSelect with the id of the clicked choice", async () => {
        const onAnswerSelect = vi.fn();
        renderQuestion({ onAnswerSelect });

        await userEvent.click(screen.getByRole("radio", { name: "Roe deer" }));

        expect(onAnswerSelect).toHaveBeenCalledWith("b");
    });

    it("disables Check Answer while no answer is selected", () => {
        renderQuestion({ selectedAnswer: null });

        expect(screen.getByRole("button", { name: "Check Answer" })).toBeDisabled();
    });

    it("shows Correct! when the selected answer is right", () => {
        renderQuestion({ selectedAnswer: "b", showFeedback: true });

        expect(screen.getByText("Correct!")).toBeInTheDocument();
    });

    it("shows Incorrect and the right answer when the selected answer is wrong", () => {
        renderQuestion({ selectedAnswer: "a", showFeedback: true });

        // /Incorrect/ is a regular expression: it finds the text that *contains* "Incorrect".
        // toHaveTextContent then checks the whole sentence the user reads, including
        // the bold part, without depending on which HTML tag makes it bold.
        expect(screen.getByText(/Incorrect/)).toHaveTextContent("The correct answer is: Roe deer");
        expect(screen.queryByText("Correct!")).not.toBeInTheDocument();
    });
});
