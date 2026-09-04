import {
    Bird,
    BookOpen,
    Dog, type LucideIcon,
    PawPrint,
    Search,
    ShieldCheck,
    Target,
    TreePine
} from "lucide-react";

export interface Subject {
    id: string;
    icon: LucideIcon;
    title: string;
    description: string;
    questionCount: number;
}

export const subject: Subject[] = [
    {
        id: "Becoming a hunter",
        icon: BookOpen,
        title: "Becoming a hunter",
        description: "Basic knowledge for those who want to become hunters.",
        questionCount: 15,
    },
    {
        id: "Hunting ethics",
        icon: BookOpen,
        title: "Hunting ethics",
        description: "Learn about ethics and responsibility in hunting.",
        questionCount: 10,
    },
    {
        id: "Ecology and wildlife management",
        icon: TreePine,
        title: "Ecology and wildlife management",
        description: "Ecology, nature, and sustainable game management.",
        questionCount: 10,
    },
    {
        id: "Cloven-hoofed game",
        icon: PawPrint,
        title: "Cloven-hoofed game",
        description: "Knowledge of hoofed game and its habitat.",
        questionCount: 10,
    },
    {
        id: "Other mammals",
        icon: PawPrint,
        title: "Other mammals",
        description: "Knowledge of large carnivores and other mammals.",
        questionCount: 10,
    },
    {
        id: "Bird identification",
        icon: Bird,
        title: "Bird identification",
        description: "Knowledge of bird and waterfowl species.",
        questionCount: 10,
    },
    {
        id: "Weapons and Shooting",
        icon: Target,
        title: "Weapons and Shooting",
        description: "Knowledge of shot, bullets, and shooting.",
        questionCount: 10,
    },
    {
        id: "Hunting dogs and hunting methods",
        icon: Dog,
        title: "Hunting dogs and hunting methods",
        description: "Hunting dogs and various hunting methods.",
        questionCount: 10,
    },
    {
        id: "Tracking / Search",
        icon: Search,
        title: "Tracking / Search",
        description: "Knowledge regarding tracking game after a shot.",
        questionCount: 10,
    },
    {
        id: "Shot placement and shots at game",
        icon: Target,
        title: "Shot placement and shots at game",
        description: "Shot placement and safe shots at game.",
        questionCount: 10,
    },
    {
        id: "The game after the shot",
        icon: PawPrint,
        title: "The game after the shot",
        description: "What happens to the game after the shot.",
        questionCount: 10,
    },
    {
        id: "The Law",
        icon: ShieldCheck,
        title: "The Law",
        description: "Laws and regulations applicable to hunting.",
        questionCount: 10,
    },
]