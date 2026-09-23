import {BookOpen, Clock, type LucideIcon, Shuffle} from "lucide-react";

export interface PracticeCards {
    icon: LucideIcon;
    iconVariant: "green" | "amber" | "blue";
    title: string;
    description: string;
    features: string[];
    buttonLabel: string;
    route: string
}

export const practiceCards: PracticeCards[] = [
    {
        icon: BookOpen,
        iconVariant: "green",
        title: "Practice by Subject",
        description: "Choose a subject area and practice questions.",
        features: ["Focus on specific topics", "Improve weak areas"],
        buttonLabel: "Select",
        route: "/practice/subjects",
    },
    {
        icon: Shuffle,
        iconVariant: "amber",
        title: "Random Practice",
        description: "Answer random questions from all subjects.",
        features: ["Randomized questions", "Improve overall knowledge"],
        buttonLabel: "Select",
        route: "/practice/random",
    },
    {
        icon: Clock,
        iconVariant: "blue",
        title: "Mock Exam",
        description: "Simulate the real exam (70 questions, timed).",
        features: ["70 questions", "Timed exam"],
        buttonLabel: "Start Exam",
        route: "/mockexam",
    },
];


