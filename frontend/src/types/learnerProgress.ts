export interface MockExamSummary {
    count: number;
    latestScore: number | null;
    latestAt: string | null;
    bestScore: number | null;
    latestPassed: boolean | null;
}

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
    }[];
    mockExams: MockExamSummary;
}

// One row from GET /api/admin/learners: admins first, then students, each by name.
export interface LearnerSummary {
    userId: number;
    fullName: string;
    role: "ADMIN" | "STUDENT";
}
