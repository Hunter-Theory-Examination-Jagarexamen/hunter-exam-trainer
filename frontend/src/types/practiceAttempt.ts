import type {ExamQuestion} from "./examQuestion";

export type OptionLetter = "A" | "B" | "C" | "D";

// Response from POST /api/practice/attempts or /api/practice/attempts/random
// (no correct answers).
export interface PracticeStartResponse {
    attemptId: number;
    // The round's subject; null for a random round, which mixes subjects.
    subjectName: string | null;
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
    answeredQuestions: number;
    totalQuestions: number;
}
