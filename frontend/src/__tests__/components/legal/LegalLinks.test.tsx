// Tests for src/components/legal/LegalLinks.tsx (#212), used on the Profile page

import { describe, it, expect } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import LegalLinks from "../../../components/legal/LegalLinks";

describe("LegalLinks", () => {

    it("opens and closes the Terms of Use pop-up", async () => {
        render(<LegalLinks />);
        expect(screen.queryByRole("dialog")).not.toBeInTheDocument();

        await userEvent.click(screen.getByRole("button", { name: "Användarvillkor" }));
        expect(screen.getByRole("dialog", { name: "Användarvillkor" })).toBeInTheDocument();
        expect(screen.getByText(/Kontot är personligt/)).toBeInTheDocument();

        await userEvent.click(screen.getByRole("button", { name: "Stäng" }));
        expect(screen.queryByRole("dialog")).not.toBeInTheDocument();
    });

    it("opens the Privacy Policy, which says what course leaders can see", async () => {
        render(<LegalLinks />);

        await userEvent.click(screen.getByRole("button", { name: "Integritetspolicy" }));

        expect(screen.getByRole("dialog", { name: "Integritetspolicy" })).toBeInTheDocument();
        expect(screen.getByText(/Kursledare \(administratörer\) ser ditt namn/)).toBeInTheDocument();
    });
});
