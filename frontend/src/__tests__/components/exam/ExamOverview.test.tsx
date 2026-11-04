import { describe, it, expect, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import ExamOverview from "../../../components/exam/ExamOverview";
import type { ExamQuestion } from "../../../types/examQuestion";

const questions: ExamQuestion[] = Array.from({ length: 5 }, (_, i) => ({
    id: i + 1,
    questionText: `Question ${i + 1}?`,
    optionA: "A", optionB: "B", optionC: "C", optionD: "D",
}));

describe("ExamOverview", () => {

    it("renders all question numbers", () => {
        render(
            <ExamOverview
                questions={questions}
                answers={{}}
                bookmarks={new Set()}
                currentIndex={0}
                onJumpToQuestion={() => {}}
            />
        );

        for (let i = 1; i <= 5; i++) {
            expect(screen.getByText(String(i))).toBeInTheDocument();
        }
    });

    it("marks the current question with aria-current", () => {
        render(
            <ExamOverview
                questions={questions}
                answers={{}}
                bookmarks={new Set()}
                currentIndex={2}
                onJumpToQuestion={() => {}}
            />
        );

        const currentBtn = screen.getByRole("button", { name: /Question 3, current/ });
        expect(currentBtn).toHaveAttribute("aria-current", "true");
    });


    it("shows answered / unanswered state", () => {
        render(
            <ExamOverview
                questions={questions}
                answers={{ "1": "A", "3": "B" }}
                bookmarks={new Set()}
                currentIndex={0}
                onJumpToQuestion={() => {}}
            />
        );

        expect(screen.getByRole("button", { name: /Question 1, current, answered/ })).toBeInTheDocument();
        expect(screen.getByRole("button", { name: /Question 2, unanswered/ })).toBeInTheDocument();
        expect(screen.getByRole("button", { name: /Question 3, answered/ })).toBeInTheDocument();
    });

    it("shows bookmarked state", () => {
        render(
            <ExamOverview
                questions={questions}
                answers={{}}
                bookmarks={new Set([2, 4])}
                currentIndex={0}
                onJumpToQuestion={() => {}}
            />
        );

        expect(screen.getByRole("button", { name: /Question 2, unanswered, bookmarked/ })).toBeInTheDocument();
        expect(screen.getByRole("button", { name: /Question 4, unanswered, bookmarked/ })).toBeInTheDocument();
    });

    it("calls onJumpToQuestion with the index when a cell is clicked", async () => {
        const onJump = vi.fn();
        render(
            <ExamOverview
                questions={questions}
                answers={{}}
                bookmarks={new Set()}
                currentIndex={0}
                onJumpToQuestion={onJump}
            />
        );

        await userEvent.click(screen.getByRole("button", { name: /Question 3,/ }));
        expect(onJump).toHaveBeenCalledWith(2);
    });
});
