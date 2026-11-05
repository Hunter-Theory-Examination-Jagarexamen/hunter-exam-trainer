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
        title: "Öva per ämne",
        description: "Välj ett ämnesområde och öva på frågor.",
        features: ["Fokusera på specifika ämnen", "Förbättra svaga områden"],
        buttonLabel: "Välj",
        route: "/practice/subjects",
    },
    {
        icon: Shuffle,
        iconVariant: "amber",
        title: "Slumpmässig övning",
        description: "Svara på slumpmässiga frågor från alla ämnen.",
        features: ["Slumpade frågor", "Förbättra din helhetskunskap"],
        buttonLabel: "Välj",
        route: "/practice/random",
    },
    {
        icon: Clock,
        iconVariant: "blue",
        title: "Övningsprov",
        description: "Simulera det riktiga provet (70 frågor, tidsbegränsat).",
        features: ["70 frågor", "Tidsbegränsat prov"],
        buttonLabel: "Starta prov",
        route: "/mockexam",
    },
];
