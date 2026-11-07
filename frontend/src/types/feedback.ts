// Feedback and question reports (see "Feedback" in docs/APIs/API-Specification.md).

export type FeedbackType = "QUESTION" | "APP" | "OTHER";

// The longest text the backend accepts.
export const FEEDBACK_MAX_LENGTH = 2000;

// Body for POST /api/feedback. questionId only when reporting a specific question.
export interface FeedbackRequest {
    type: FeedbackType;
    text: string;
    questionId?: number;
}

// One item from GET /api/admin/feedback. There is no sender: admins don't see who sent it.
export interface FeedbackItem {
    id: number;
    type: FeedbackType;
    text: string;
    createdAt: string;
    handled: boolean;
    // null for general feedback, or when the reported question has been deleted.
    questionId: number | null;
    // The question's text when it was reported (null for general feedback).
    questionText: string | null;
}

// Optional filters for the admin list; a missing filter means "any".
export interface FeedbackFilter {
    type?: FeedbackType;
    handled?: boolean;
}
