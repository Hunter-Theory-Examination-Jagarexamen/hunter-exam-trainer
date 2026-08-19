
export interface AnswerOption {
    id: string;
    text: string;
    isCorrect: boolean;
}

export interface Question {
    id: string;
    subjectArea: string;
    question: string;
    options: AnswerOption[];
}

export const mockQuestions: Question[] = [
    {
        id: "q1",
        subjectArea: "Hunting Legislation",
        question: "What is the minimum age requirement to obtain a hunting license in Sweden?",
        options: [
            { id: "a", text: "16 years", isCorrect: false},
            { id: "b", text: "18 years", isCorrect: true},
            { id: "c", text: "20 years", isCorrect: false},
            { id: "d", text: "21 years", isCorrect: false},
        ],
    },
    {
        id: "q2",
        subjectArea: "Hunting Ethics and Wildlife Conservation",
        question: "What is the primary ethical responsibility of a hunter when an animal is wounded but not killed?",
        options: [
            { id: "a", text: "Leave it, as it will likely recover on its own", isCorrect: false},
            { id: "b", text: "Report it to the police and take no further action", isCorrect: false},
            { id: "c", text: "Track and dispatch the animal as quickly as possible", isCorrect: true},
            { id: "d", text: "Wait 24 hours before searching for it", isCorrect: false},
        ],
    },
    {
        id: "q3",
        subjectArea: "Ecology",
        question: "Which of the following best describes a 'keystone species' in an ecosystem?",
        options: [
            { id: "a", text: "The most numerous species in the area", isCorrect: false},
            { id: "b", text: "A species whose impact on its environment is disproportionately large relative to its abundance", isCorrect: true},
            { id: "c", text: "A species that only eats plants", isCorrect: false},
            { id: "d", text: "The largest predator in the food chain", isCorrect: false},
        ],
    },
    {
        id: "q4",
        subjectArea: "Species Knowledge - Mammals",
        question: "Which of these mammals is classified as a cloven-hoofed (even-toed) ungulate?",
        options: [
            { id: "a", text: "Red fox", isCorrect: false},
            { id: "b", text: "Roe deer", isCorrect: true},
            { id: "c", text: "European badger", isCorrect: false},
            { id: "d", text: "Eurasian lynx", isCorrect: false},
        ],
    },
    {
        id: "q5",
        subjectArea: "Weapons and Shooting",
        question: "What is the primary purpose of checking a rifle's zero before a hunt?",
        options: [
            { id: "a", text: "To confirm the point of impact matches the point of aim at a given distance", isCorrect: true},
            { id: "b", text: "To clean the barrel of residue", isCorrect: false},
            { id: "c", text: "To reduce the weight of the rifle", isCorrect: false},
            { id: "d", text: "To test the trigger pull weight", isCorrect: false},
        ],
    },
]

