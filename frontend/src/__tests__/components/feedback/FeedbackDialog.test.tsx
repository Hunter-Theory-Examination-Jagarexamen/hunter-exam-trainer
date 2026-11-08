// Tests for src/components/feedback/FeedbackDialog.tsx
//
// apiClient is mocked, so each test checks what the dialog sends to the backend
// and what the student sees afterwards.

import { beforeEach, describe, expect, it, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import apiClient from "../../../api/apiClient";
import FeedbackDialog from "../../../components/feedback/FeedbackDialog";

vi.mock("../../../api/apiClient", () => ({ default: vi.fn() }));
const apiClientMock = vi.mocked(apiClient);

// Same reset as the admin page tests. apiClientMock.mockReset() alone made the 429 test
// fail with its own (caught) error under Vitest 5; resetAllMocks() does not.
beforeEach(() => vi.resetAllMocks());

// The body of the last request, as an object.
const sentBody = () => JSON.parse(apiClientMock.mock.lastCall![1]!.body as string);

describe("FeedbackDialog", () => {
    it("reports a question with its id and shows a confirmation", async () => {
        apiClientMock.mockResolvedValue("");
        render(<FeedbackDialog onClose={vi.fn()} initialType="QUESTION" questionId={12} />);

        expect(screen.getByLabelText("Vad gäller det?")).toHaveValue("QUESTION");
        await userEvent.type(screen.getByLabelText("Ditt meddelande"), "Fel svar markerat som rätt");
        await userEvent.click(screen.getByRole("button", { name: "Skicka" }));

        expect(apiClientMock).toHaveBeenCalledWith("/api/feedback", expect.objectContaining({ method: "POST" }));
        expect(sentBody()).toEqual({ type: "QUESTION", text: "Fel svar markerat som rätt", questionId: 12 });
        expect(await screen.findByRole("status"))
            .toHaveTextContent("Tack! Ditt meddelande har skickats till kursledarna.");
    });

    it("sends general feedback without a question", async () => {
        apiClientMock.mockResolvedValue("");
        render(<FeedbackDialog onClose={vi.fn()} />);

        expect(screen.getByLabelText("Vad gäller det?")).toHaveValue("APP");
        await userEvent.type(screen.getByLabelText("Ditt meddelande"), "  Bra app  ");
        await userEvent.click(screen.getByRole("button", { name: "Skicka" }));

        expect(sentBody()).toEqual({ type: "APP", text: "Bra app" });
    });

    it("leaves out the question when the student changes the type", async () => {
        apiClientMock.mockResolvedValue("");
        render(<FeedbackDialog onClose={vi.fn()} initialType="QUESTION" questionId={12} />);

        await userEvent.selectOptions(screen.getByLabelText("Vad gäller det?"), "OTHER");
        await userEvent.type(screen.getByLabelText("Ditt meddelande"), "Något annat");
        await userEvent.click(screen.getByRole("button", { name: "Skicka" }));

        expect(sentBody()).toEqual({ type: "OTHER", text: "Något annat" });
    });

    it("can't send an empty message and counts the characters", async () => {
        render(<FeedbackDialog onClose={vi.fn()} />);

        expect(screen.getByRole("button", { name: "Skicka" })).toBeDisabled();
        expect(screen.getByLabelText("Ditt meddelande")).toHaveAttribute("maxLength", "2000");
        await userEvent.type(screen.getByLabelText("Ditt meddelande"), "Hej");
        expect(screen.getByText("3 / 2000")).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "Skicka" })).toBeEnabled();
    });

    it("explains the daily limit and keeps the text", async () => {
        apiClientMock.mockRejectedValue(Object.assign(new Error('{"message":"Too much"}'), { status: 429 }));
        render(<FeedbackDialog onClose={vi.fn()} />);

        await userEvent.type(screen.getByLabelText("Ditt meddelande"), "Hej");
        await userEvent.click(screen.getByRole("button", { name: "Skicka" }));

        expect(await screen.findByRole("alert"))
            .toHaveTextContent("Du har skickat många meddelanden i dag. Försök igen i morgon.");
        expect(screen.getByLabelText("Ditt meddelande")).toHaveValue("Hej");
    });

    it("closes with the cancel button and with Escape", async () => {
        const onClose = vi.fn();
        render(<FeedbackDialog onClose={onClose} />);

        await userEvent.click(screen.getByRole("button", { name: "Avbryt" }));
        await userEvent.keyboard("{Escape}");

        expect(onClose).toHaveBeenCalledTimes(2);
    });
});
