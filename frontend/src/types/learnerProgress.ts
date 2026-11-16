export interface MockExamSummary {
    count: number;
    latestScore: number | null;
    latestAt: string | null;
    bestScore: number | null;
    latestPassed: boolean | null;
}

// One row from GET /api/admin/learners/{userId}.
// Sharing (#125): subjectPerformances and mockExams are null when the student
// has not shared that category (never an empty stand-in), and lastActivityAt
// only covers shared categories.
export interface LearnerProgress {
    userId: number;
    fullName: string;
    email: string;
    role: "STUDENT" | "ADMIN";
    createdAt: string;
    lastActivityAt: string | null;
    subjectPerformances: {
        subjectId: number;
        subjectName: string;
        percentage: number;
    }[] | null;
    mockExams: MockExamSummary | null;
}

// One row from GET /api/admin/learners: admins first, then students, each by name.
export interface LearnerSummary {
    userId: number;
    fullName: string;
    role: "ADMIN" | "STUDENT";
}
