// Tests for src/components/legal/LegalDialog.tsx (#212)

import { describe, it, expect, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import LegalDialog from "../../../components/legal/LegalDialog";

function renderDialog(onClose = vi.fn()) {
    render(
        <LegalDialog title="Användarvillkor" onClose={onClose}>
            <p>Some legal text</p>
        </LegalDialog>
    );
    return onClose;
}

describe("LegalDialog", () => {

    it("shows the title, the temporary note and the text", () => {
        renderDialog();

        expect(screen.getByRole("dialog", { name: "Användarvillkor" })).toBeInTheDocument();
        expect(screen.getByText(/Tillfällig version/)).toBeInTheDocument();
        expect(screen.getByText("Some legal text")).toBeInTheDocument();
    });

    it("closes with the close button", async () => {
        const onClose = renderDialog();

        await userEvent.click(screen.getByRole("button", { name: "Stäng" }));

        expect(onClose).toHaveBeenCalledTimes(1);
    });

    it("closes with the Escape key", async () => {
        const onClose = renderDialog();

        await userEvent.keyboard("{Escape}");

        expect(onClose).toHaveBeenCalledTimes(1);
    });

    it("closes on a click outside the card, but not inside it", async () => {
        const onClose = renderDialog();
        const dialog = screen.getByRole("dialog");

        await userEvent.click(screen.getByText("Some legal text"));
        expect(onClose).not.toHaveBeenCalled();

        // The backdrop is the element around the card.
        await userEvent.click(dialog.parentElement!);
        expect(onClose).toHaveBeenCalledTimes(1);
    });
});
