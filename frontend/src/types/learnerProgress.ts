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
    createdAt: string;
    lastActivityAt: string | null;
    subjectPerformances: {
        subjectId: number;
        subjectName: string;
        percentage: number;
    }[];
    mockExams: MockExamSummary;
}