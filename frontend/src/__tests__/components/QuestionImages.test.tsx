import {describe, expect, it, vi} from "vitest";
import {fireEvent, render, screen} from "@testing-library/react";
import {createMemoryRouter, RouterProvider} from "react-router-dom";
import QuestionImage from "../../components/common/QuestionImage";
import QuestionPanel from "../../components/exam/QuestionPanel";
import PracticeQuestions from "../../pages/Practice/PracticeQuestions";
import apiClient from "../../api/apiClient";
import type {Question} from "../../types/question";
import imageStyles from "../../styles/question-image.css?inline";
import "../../styles/question-image.css";

vi.mock("../../api/apiClient", () => ({default: vi.fn()}));
const question: Question = {id: 1, questionText: "Which animal?", optionA: "Fox", optionB: "Deer",
    optionC: "Moose", optionD: "Bear", correctAnswer: "Fox", explanation: "",
    subject: {id: 1, name: "Wildlife", description: ""}, imageUrl: "https://example.com/fox.jpg"};

describe("Question images", () => {
    it.each([undefined, null, "", "   ", "not a URL", "javascript:alert(1)", "data:image/png;base64,abc"])(
        "does not render absent or invalid URL %s", imageUrl => {
            render(<QuestionImage imageUrl={imageUrl} questionText="Which animal?" />);
            expect(screen.queryByRole("img")).not.toBeInTheDocument();
            expect(screen.queryByRole("status")).not.toBeInTheDocument();
        });

    it("uses contextual alt text and responsive styling, and retries when the URL changes", () => {
        // Vitest does not attach imported stylesheets to jsdom automatically.
        const {rerender} = render(<><style>{imageStyles}</style>
            <QuestionImage imageUrl={question.imageUrl} questionText={question.questionText} /></>);
        const image = screen.getByRole("img", {name: "Image for question: Which animal?"});
        expect(image).toHaveClass("question-image");
        expect(getComputedStyle(image).maxWidth).toBe("100%");
        expect(getComputedStyle(image).height).toBe("auto");
        expect(getComputedStyle(image).objectFit).toBe("contain");
        fireEvent.error(image);
        expect(screen.queryByRole("img")).not.toBeInTheDocument();
        expect(screen.getByRole("status")).toHaveTextContent("Question image unavailable.");
        rerender(<QuestionImage imageUrl="https://example.com/deer.jpg" questionText="Which deer?" />);
        expect(screen.getByRole("img", {name: "Image for question: Which deer?"})).toHaveAttribute("src", "https://example.com/deer.jpg");
    });

    it("displays the image in mock exams and preserves answers after loading failure", () => {
        const onAnswerChange = vi.fn();
        render(<QuestionPanel question={question} onAnswerChange={onAnswerChange} />);
        expect(screen.getByRole("img")).toHaveAttribute("src", question.imageUrl);
        fireEvent.error(screen.getByRole("img"));
        fireEvent.click(screen.getByRole("radio", {name: "Fox"}));
        expect(onAnswerChange).toHaveBeenCalledWith("A");
        expect(screen.getByRole("heading", {name: question.questionText})).toBeInTheDocument();
    });

    it.each([question.imageUrl, null])("maps API images through the practice page: %s", async imageUrl => {
        // POST /api/practice/attempts returns a round; its questions have no correct answer.
        const {id, questionText, optionA, optionB, optionC, optionD} = question;
        vi.mocked(apiClient).mockResolvedValueOnce({attemptId: 10, subjectName: "Wildlife",
            questions: [{id, questionText, optionA, optionB, optionC, optionD, imageUrl}]});
        // A data router, because the practice page uses useBlocker.
        render(<RouterProvider router={createMemoryRouter(
            [{path: "/practice/:subject", element: <PracticeQuestions />}], {initialEntries: ["/practice/1"]})} />);
        await screen.findByRole("heading", {name: question.questionText});
        if (imageUrl) {
            expect(screen.getByRole("img", {name: "Image for question: Which animal?"})).toHaveAttribute("src", imageUrl);
            fireEvent.error(screen.getByRole("img"));
            expect(screen.queryByRole("img")).not.toBeInTheDocument();
        } else expect(screen.queryByRole("img")).not.toBeInTheDocument();
        expect(screen.getByRole("radio", {name: "Fox"})).toBeInTheDocument();
    });
});
