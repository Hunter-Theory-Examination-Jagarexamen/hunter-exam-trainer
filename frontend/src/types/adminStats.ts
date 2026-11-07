export interface AdminStats {
    questionCount: number;
    subjectCount: number;
    studentCount: number;
    // Feedback items not yet marked as handled.
    unhandledFeedbackCount: number;
}
