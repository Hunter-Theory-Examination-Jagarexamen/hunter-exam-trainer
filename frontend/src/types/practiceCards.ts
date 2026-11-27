import {BookOpen, Clock, type LucideIcon, Shuffle} from "lucide-react";

export interface PracticeCards {
    icon: LucideIcon;
    mode: "subject" | "random" | "exam";   // picks the card colour (practice.css)
    title: string;
    description: string;
    features: string[];
    buttonLabel: string;
    route: string
}

export const practiceCards: PracticeCards[] = [
    {
        icon: BookOpen,
        mode: "subject",
        title: "practiceCards.subject.title",
        description: "practiceCards.subject.description",
        features: ["practiceCards.subject.feature1", "practiceCards.subject.feature2"],
        buttonLabel: "practiceCards.subject.button",
        route: "/practice/subjects",
    },
    {
        icon: Shuffle,
        mode: "random",
        title: "practiceCards.random.title",
        description: "practiceCards.random.description",
        features: ["practiceCards.random.feature1", "practiceCards.random.feature2"],
        buttonLabel: "practiceCards.random.button",
        route: "/practice/random",
    },
    {
        icon: Clock,
        mode: "exam",
        title: "practiceCards.mockExam.title",
        description: "practiceCards.mockExam.description",
        features: ["practiceCards.mockExam.feature1", "practiceCards.mockExam.feature2"],
        buttonLabel: "practiceCards.mockExam.button",
        route: "/mockexam",
    },
];
