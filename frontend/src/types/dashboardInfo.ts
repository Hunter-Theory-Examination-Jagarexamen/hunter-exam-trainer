export interface DashboardInfo {

    mockExams: number;
    questionsAnswered: number;
    averageScore: number;
    bestScore: number;
}

export interface RecentActivity {

    id: number;
    correctAnswers: number;
    totalQuestions: number;
    score: number;
    completedAt: string;
}