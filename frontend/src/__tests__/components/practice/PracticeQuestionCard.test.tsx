// Tests for src/components/practice/PracticeQuestionCard.tsx
//
// PracticeQuestionCard is a "controlled" component: the parent page decides which
// answer is selected and whether feedback is shown, and passes that in as props.
// So each test draws it with the props it wants and checks what the user sees.

import { describe, it, expect, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import PracticeQuestionCard from "../../../components/practice/PracticeQuestionCard";
import type { PracticeQuestion } from "../../../types/practiceQuestions";

vi.mock("../../../api/apiClient.ts", () => ({
    default: vi.fn(),
}));

// A small made-up question. Option ids are letters, as on the practice page.
// Which option is correct comes from the server's reply (see the feedback objects below).
const question: PracticeQuestion = {
    id: "q1",
    subjectArea: "Test subject",
    question: "Which animal is a deer?",
    options: [
        { id: "A", answer: "Wild boar", isCorrect: false },
        { id: "B", answer: "Roe deer", isCorrect: false },
        { id: "C", answer: "Fox", isCorrect: false },
    ],
};

// Server replies: B is the correct option. isCorrect above is false everywhere on
// purpose, to prove the card uses the server's reply and not the option data.
const rightAnswer = { correct: true, correctOption: "B", explanation: null } as const;
const wrongAnswer = { correct: false, correctOption: "B", explanation: null } as const;

// Draw the component. Defaults: nothing selected, no feedback, mock callbacks.
// A test overrides only the props it cares about.
function renderQuestion(props: Partial<React.ComponentProps<typeof PracticeQuestionCard>> = {}) {
    render(
        <PracticeQuestionCard
            question={question}
            selectedAnswer={null}
            feedback={null}
            onAnswerSelect={vi.fn()}
            onCheckAnswer={vi.fn()}
            {...props}
        />
    );
}

describe("PracticeQuestionCard", () => {

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

        expect(onAnswerSelect).toHaveBeenCalledWith("B");
    });

    it("disables Check Answer while no answer is selected", () => {
        renderQuestion({ selectedAnswer: null });

        expect(screen.getByRole("button", { name: "Check Answer" })).toBeDisabled();
    });

    it("shows Correct! when the selected answer is right", () => {
        renderQuestion({ selectedAnswer: "B", feedback: rightAnswer });

        expect(screen.getByText("Correct!")).toBeInTheDocument();
    });

    it("shows Incorrect and the right answer when the selected answer is wrong", () => {
        renderQuestion({ selectedAnswer: "A", feedback: wrongAnswer });

        // /Incorrect/ is a regular expression: it finds the text that *contains* "Incorrect".
        // toHaveTextContent then checks the whole sentence the user reads, including
        // the bold part, without depending on which HTML tag makes it bold.
        expect(screen.getByText(/Incorrect/)).toHaveTextContent("The correct answer is: Roe deer");
        expect(screen.queryByText("Correct!")).not.toBeInTheDocument();
    });

    it("moves focus to the feedback so screen readers read it", () => {
        renderQuestion({ selectedAnswer: "B", feedback: rightAnswer });

        // document.activeElement is the element that has focus right now.
        // Screen readers read the focused element, so the feedback must have it.
        // If someone removes the focus code, sighted users see no difference,
        // so this test guards it.
        expect(document.activeElement).toHaveTextContent("Correct!");
    });
});
