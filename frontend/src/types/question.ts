export interface Question {

    id: number;
    questionText: string;
    optionA: string;
    optionB: string;
    optionC: string;
    optionD: string;
    correctAnswer: string;
    explanation: string;
    subject: {
        id: number;
        name: string;
        description: string;
    }
}