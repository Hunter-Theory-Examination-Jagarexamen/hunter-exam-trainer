export interface LearnerProgress {
    userId: number;
    fullName: string;
    email: string;
    createdAt: string;
    lastPracticedAt: string | null;
    subjectPerformances: {
        subjectId: number;
        subjectName: string;
        percentage: number;
    }[];
}