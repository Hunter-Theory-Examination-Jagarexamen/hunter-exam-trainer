import {
    BookOpen,
    ShieldCheck,
    TreePine,
    PawPrint,
    Bird,
    Target,
    Crosshair,
    Dog,
    Search,
    FileText,
    RotateCcw,
    type LucideIcon
} from "lucide-react";

export const subjectIcons: Record<string, LucideIcon> = {
    "Introduktion": BookOpen,
    "Jaktetik": ShieldCheck,
    "Ekologi": TreePine,
    "Klövvilt": PawPrint,
    "Övriga däggdjur": PawPrint,
    "Fåglar: gäss & änder": Bird,
    "Fåglar: övriga": Bird,
    "Vapen: hagel": Target,
    "Vapen: kula": Crosshair,
    "Träffområden": Target,
    "Jakthundar": Dog,
    "Eftersök": Search,
    "Viltet efter skottet": FileText,
    "Lagen": ShieldCheck,
    "Repetition": RotateCcw
};