// Tests for the mock exam instructions (components/mockexam/ExamInstructions,
// rendered by the MockExam page): all rules are translated, and the page states
// that unanswered questions count as incorrect (#186).

import { describe, it, expect } from "vitest";
import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import MockExam from "../../../pages/MockExam/MockExam";

describe("Mock exam instructions", () => {

    it("lists every exam rule, including that unanswered questions count as incorrect", () => {
        render(
            <MemoryRouter>
                <MockExam />
            </MemoryRouter>
        );

        expect(screen.getByRole("heading", { name: "Instruktioner" })).toBeInTheDocument();
        expect(screen.getByText("Provet innehåller 70 flervalsfrågor.")).toBeInTheDocument();
        expect(screen.getByText("Du har 60 minuter på dig att slutföra provet.")).toBeInTheDocument();
        expect(screen.getByText("Varje fråga har endast ett rätt svar.")).toBeInTheDocument();
        expect(screen.getByText("Du kan granska dina svar innan du lämnar in.")).toBeInTheDocument();
        expect(screen.getByText("Antal rätt svar visas efter inlämning.")).toBeInTheDocument();
        expect(screen.getByText("Du behöver minst 60 rätt för godkänt.")).toBeInTheDocument();
        expect(screen.getByText("Obesvarade frågor räknas som fel.")).toBeInTheDocument();
        expect(screen.getByText("Felaktiga svar ger inga minuspoäng.")).toBeInTheDocument();
    });

});
