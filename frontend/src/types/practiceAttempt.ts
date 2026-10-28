import type {ExamQuestion} from "./examQuestion";

export type OptionLetter = "A" | "B" | "C" | "D";

// Response from POST /api/practice/attempts (no correct answers).
export interface PracticeStartResponse {
    attemptId: number;
    questions: ExamQuestion[];
}

// Response from POST /api/practice/attempts/{id}/answers: the server's check.
export interface PracticeAnswerResponse {
    correct: boolean;
    correctOption: OptionLetter;
    explanation: string | null;
}

// Response from POST /api/practice/attempts/{id}/finish, counted by the server.
export interface PracticeFinishResponse {
    correctAnswers: number;
    totalQuestions: number;
}
