// Which part of the app (section) a page belongs to. The section decides the
// colour of the cards on that page (see .main-content[data-section] in layout.css).
// Pages not listed here (Hem, feedback, the practice mode choice) keep the green cards.

export type PageSection = "quiz" | "random" | "exam" | "results" | "profile" | "admin";

const sectionRoutes: { path: string; section: PageSection }[] = [
    { path: "/practice/subjects", section: "quiz" },
    { path: "/practice/random", section: "random" },
    { path: "/mockexam", section: "exam" },
    { path: "/exam", section: "exam" },
    { path: "/result", section: "exam" },
    { path: "/statistics", section: "results" },
    { path: "/profile", section: "profile" },
    { path: "/admin", section: "admin" },
];

// A path matches its own route and every route below it:
// "/admin" matches "/admin" and "/admin/learners/5", but not "/administration".
export function sectionForPath(pathname: string): PageSection | undefined {
    const match = sectionRoutes.find(
        ({ path }) => pathname === path || pathname.startsWith(path + "/"),
    );
    return match?.section;
}
