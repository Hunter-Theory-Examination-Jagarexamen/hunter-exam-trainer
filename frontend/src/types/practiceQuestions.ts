export interface PracticeAnswerOption {
    id: string;
    answer: string;
    isCorrect: boolean;
}

export interface PracticeQuestion {
    id: string;
    subjectArea: string;
    question: string;
    options: PracticeAnswerOption[];
    imageUrl?: string;
}

export const practiceQuestions: PracticeQuestion[] = [
    {
        id: "p1",
        subjectArea: "Becoming a hunter",
        question: "What is an important responsibility of a hunter?",
        imageUrl: "https://example.com/images/hunter-responsibility.jpg",
        options: [
            {
                id: "a",
                answer: "To hunt as many animals as possible",
                isCorrect: false,
            },
            {
                id: "b",
                answer: "To hunt responsibly and respect wildlife",
                isCorrect: true,
            },
            {
                id: "c",
                answer: "To avoid following hunting regulations",
                isCorrect: false,
            },
            {
                id: "d",
                answer: "To hunt regardless of weather conditions",
                isCorrect: false,
            },
        ],
    },
    {
        id: "p2",
        subjectArea: "Becoming a hunter",
        question: "What should a hunter do when an animal is wounded?",
        options: [
            {
                id: "a",
                answer: "Leave the area immediately",
                isCorrect: false,
            },
            {
                id: "b",
                answer: "Wait until the next day",
                isCorrect: false,
            },
            {
                id: "c",
                answer: "Carry out an appropriate search for the animal",
                isCorrect: true,
            },
            {
                id: "d",
                answer: "Ignore the situation",
                isCorrect: false,
            },
        ],
    },
];