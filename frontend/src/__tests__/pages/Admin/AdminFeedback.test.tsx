// Tests for src/pages/Admin/AdminFeedback.tsx (the /admin/feedback page).
// apiClient is mocked; each test checks which list the page asks for and what the admin sees.

import { beforeEach, describe, expect, it, vi } from "vitest";
import { render, screen, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import apiClient from "../../../api/apiClient";
import AdminFeedback from "../../../pages/Admin/AdminFeedback";
import type { FeedbackItem } from "../../../types/feedback";

vi.mock("../../../api/apiClient", () => ({ default: vi.fn() }));
const apiClientMock = vi.mocked(apiClient);

const report: FeedbackItem = {
    id: 1, type: "QUESTION", text: "Fel svar markerat som rätt", createdAt: "2026-11-06T10:15:00",
    handled: false, questionId: 12, questionText: "Vilket djur är ett hjortdjur?",
};
const deletedQuestionReport: FeedbackItem = {
    ...report, id: 2, text: "Bilden saknas", questionId: null, questionText: "Gammal fråga?",
};
const appFeedback: FeedbackItem = {
    id: 3, type: "APP", text: "Bra app", createdAt: "2026-11-05T09:00:00",
    handled: false, questionId: null, questionText: null,
};

beforeEach(() => vi.resetAllMocks());

describe("AdminFeedback page", () => {
    it("shows unhandled feedback by default, with the reported question", async () => {
        apiClientMock.mockResolvedValue([report, appFeedback]);
        render(<MemoryRouter><AdminFeedback /></MemoryRouter>);

        expect(await screen.findByText("Fel svar markerat som rätt")).toBeInTheDocument();
        expect(apiClientMock).toHaveBeenCalledWith("/api/admin/feedback?handled=false");
        expect(screen.getByText("Vilket djur är ett hjortdjur?")).toBeInTheDocument();
        expect(screen.getByRole("link", { name: "Öppna frågan" })).toHaveAttribute("href", "/admin/questions?edit=12");
        expect(screen.getByText("Bra app")).toBeInTheDocument();
        expect(screen.getAllByRole("listitem")).toHaveLength(2);
    });

    it("says when a reported question has been deleted", async () => {
        apiClientMock.mockResolvedValue([deletedQuestionReport]);
        render(<MemoryRouter><AdminFeedback /></MemoryRouter>);

        expect(await screen.findByText("Gammal fråga?")).toBeInTheDocument();
        expect(screen.getByText("(frågan är borttagen)")).toBeInTheDocument();
        expect(screen.queryByRole("link", { name: "Öppna frågan" })).not.toBeInTheDocument();
    });

    it("asks for the chosen type, and for handled items too when shown", async () => {
        apiClientMock.mockResolvedValue([]);
        render(<MemoryRouter><AdminFeedback /></MemoryRouter>);
        await screen.findByText("Ingen feedback att visa.");

        await userEvent.selectOptions(screen.getByLabelText("Typ"), "QUESTION");
        expect(apiClientMock).toHaveBeenLastCalledWith("/api/admin/feedback?type=QUESTION&handled=false");

        await userEvent.click(screen.getByLabelText("Visa hanterade"));
        expect(apiClientMock).toHaveBeenLastCalledWith("/api/admin/feedback?type=QUESTION");
    });

    it("removes an item from the unhandled list when it is marked as handled", async () => {
        apiClientMock.mockImplementation(async (endpoint: string) =>
            endpoint === "/api/admin/feedback/1/handled" ? { ...report, handled: true } : [report, appFeedback]);
        render(<MemoryRouter><AdminFeedback /></MemoryRouter>);

        const item = (await screen.findByText("Fel svar markerat som rätt")).closest("li")!;
        await userEvent.click(within(item).getByRole("button", { name: "Markera som hanterad" }));

        expect(apiClientMock).toHaveBeenCalledWith("/api/admin/feedback/1/handled",
            { method: "PATCH", body: JSON.stringify({ handled: true }) });
        expect(screen.queryByText("Fel svar markerat som rätt")).not.toBeInTheDocument();
        expect(screen.getByText("Bra app")).toBeInTheDocument();
    });

    it("keeps a handled item visible, with an undo, when handled items are shown", async () => {
        const handledReport = { ...report, handled: true };
        apiClientMock.mockImplementation(async (endpoint: string) =>
            endpoint === "/api/admin/feedback/1/handled" ? { ...report, handled: false } : [handledReport]);
        render(<MemoryRouter><AdminFeedback /></MemoryRouter>);
        await userEvent.click(screen.getByLabelText("Visa hanterade"));

        const button = await screen.findByRole("button", { name: "Markera som ohanterad" });
        await userEvent.click(button);

        expect(await screen.findByRole("button", { name: "Markera som hanterad" })).toBeInTheDocument();
    });

    it("shows an error when the list can't be loaded", async () => {
        apiClientMock.mockImplementation(() => Promise.reject(Object.assign(new Error("boom"), { status: 500 })));
        render(<MemoryRouter><AdminFeedback /></MemoryRouter>);

        expect(await screen.findByRole("alert"))
            .toHaveTextContent("Det gick inte att ladda feedback. Försök igen.");
    });
});
