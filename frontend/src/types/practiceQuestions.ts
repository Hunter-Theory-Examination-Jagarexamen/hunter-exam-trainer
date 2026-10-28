export interface PracticeAnswerOption {
    id: string;
    answer: string;
}

export interface PracticeQuestion {
    id: string;
    subjectArea: string;
    question: string;
    options: PracticeAnswerOption[];
    imageUrl?: string | null;
}
