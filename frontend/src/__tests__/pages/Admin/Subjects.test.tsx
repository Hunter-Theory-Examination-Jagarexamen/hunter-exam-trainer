import {beforeEach, describe, expect, it, vi} from "vitest";
import {act, fireEvent, render, screen} from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import Subjects from "../../../pages/Admin/Subjects";
import apiClient from "../../../api/apiClient";

vi.mock("../../../api/apiClient", () => ({default: vi.fn()}));

const httpError = (status: number, body: string) => Object.assign(new Error(body), {status});
const subject = {id: 1, name: "Safety", description: "Safe hunting", questionCount: 0};
const mutations = () => vi.mocked(apiClient).mock.calls.filter(([, options]) => options?.method);
beforeEach(() => { vi.resetAllMocks(); vi.mocked(apiClient).mockResolvedValue([subject]); });

async function open(mode: "create" | "edit" | "delete") {
    render(<Subjects />);
    await screen.findByRole("heading", {name: "Safety"});
    await userEvent.click(screen.getByRole("button", {name: mode === "create" ? "Skapa ämne" : `${mode === "edit" ? "Redigera" : "Ta bort"} ämne Safety`}));
}
function fill(name = "Wildlife", description = "Wildlife description") {
    fireEvent.change(screen.getByLabelText("Namn (obligatoriskt)"), {target: {value: name}});
    fireEvent.change(screen.getByLabelText("Beskrivning (obligatoriskt)"), {target: {value: description}});
}

describe("Subject management", () => {
    it("loads names, descriptions and question counts", async () => {
        render(<Subjects />);
        expect(screen.getByRole("status")).toHaveTextContent("Laddar ämnen...");
        expect(await screen.findByRole("heading", {name: "Safety"})).toBeInTheDocument();
        expect(screen.getByText("Safe hunting")).toBeInTheDocument();
        expect(screen.getByText("0 frågor")).toBeInTheDocument();
        expect(apiClient).toHaveBeenCalledWith("/api/subjects");
    });
    it("shows an empty state and allows creation", async () => {
        vi.mocked(apiClient).mockResolvedValue([]);
        render(<Subjects />);
        expect(await screen.findByText(/Inga ämnen tillgängliga/)).toBeInTheDocument();
        await userEvent.click(screen.getByRole("button", {name: "Skapa ämne"}));
        expect(screen.getByRole("form", {name: "Skapa ämne"})).toBeInTheDocument();
        expect(screen.getByLabelText("Namn (obligatoriskt)")).toHaveFocus();
    });
    it("shows a safe load failure and retries", async () => {
        vi.mocked(apiClient).mockRejectedValueOnce(new Error("SQL secret"));
        render(<Subjects />);
        expect(await screen.findByRole("alert")).toHaveTextContent("Failed to load subjects. Please try again.");
        await userEvent.click(screen.getByRole("button", {name: "Försök ladda ämnen igen"}));
        expect(await screen.findByRole("heading", {name: "Safety"})).toBeInTheDocument();
    });
    it.each([
        ["", "description", "Ange ett ämnesnamn."],
        ["   ", "description", "Ange ett ämnesnamn."],
        ["Name", "   ", "Ange en ämnesbeskrivning."],
        ["x".repeat(256), "description", "Namn och beskrivning får högst vara 255 tecken var."],
        ["Name", "x".repeat(256), "Namn och beskrivning får högst vara 255 tecken var."]
    ])("validates input before sending", async (name, description, message) => {
        await open("create"); fill(name, description);
        await userEvent.click(screen.getByRole("button", {name: "Spara ämne"}));
        expect(screen.getByRole("alert")).toHaveTextContent(message);
        expect(mutations()).toHaveLength(0);
    });
    it("creates using the required payload and immediately displays the server response", async () => {
        await open("create"); fill(" Wildlife ", " Wildlife description ");
        vi.mocked(apiClient).mockResolvedValueOnce({...subject, id: 2, name: "Wildlife", description: "Wildlife description"});
        await userEvent.click(screen.getByRole("button", {name: "Spara ämne"}));
        expect(apiClient).toHaveBeenCalledWith("/api/admin/subjects", {method: "POST", body: JSON.stringify({name: "Wildlife", description: "Wildlife description"})});
        expect(await screen.findByRole("heading", {name: "Wildlife"})).toBeInTheDocument();
        expect(screen.getByRole("status")).toHaveTextContent('Ämne "Wildlife" skapades.');
        expect(screen.getByRole("button", {name: "Skapa ämne"})).toHaveFocus();
    });
    it("prepopulates and updates both fields using PUT", async () => {
        await open("edit");
        expect(screen.getByLabelText("Namn (obligatoriskt)")).toHaveValue("Safety");
        expect(screen.getByLabelText("Beskrivning (obligatoriskt)")).toHaveValue("Safe hunting");
        fill("Renamed", "New description");
        vi.mocked(apiClient).mockResolvedValueOnce({...subject, name: "Renamed", description: "New description"});
        await userEvent.click(screen.getByRole("button", {name: "Spara ämne"}));
        expect(apiClient).toHaveBeenCalledWith("/api/admin/subjects/1", {method: "PUT", body: JSON.stringify({name: "Renamed", description: "New description"})});
        expect(await screen.findByRole("heading", {name: "Renamed"})).toBeInTheDocument();
        expect(screen.getByText("New description")).toBeInTheDocument();
        expect(screen.queryByRole("heading", {name: "Safety"})).not.toBeInTheDocument();
        expect(screen.getByRole("status")).toHaveTextContent("uppdaterades");
    });
    it.each(["create", "edit"] as const)("translates a 409 duplicate name on %s and preserves inputs", async mode => {
        await open(mode); fill();
        vi.mocked(apiClient).mockRejectedValueOnce(httpError(409, '{"message":"This change conflicts with existing data"}'));
        await userEvent.click(screen.getByRole("button", {name: "Spara ämne"}));
        expect(await screen.findByRole("alert")).toHaveTextContent("A subject with this name already exists. Choose a different name.");
        expect(screen.getByLabelText("Namn (obligatoriskt)")).toHaveValue("Wildlife");
    });
    it.each([
        ["404", httpError(404, '{"message":"Subject not found with id: 1"}'), "This subject no longer exists. Refresh the subject list before trying again."],
        ["403", httpError(403, "API request failed: 403"), "You do not have permission to manage subjects. Please sign in with an admin account."],
        ["network failure", new Error("Failed to fetch"), "The request failed. Please try again. If it persists, contact an administrator."],
        ["400 required", httpError(400, '{"message":"Description is required"}'), "Enter a subject description."],
        ["400 too long", httpError(400, '{"message":"Name must be at most 255 characters"}'), "Name must be at most 255 characters"],
        ["500 with code", httpError(500, '{"message":"An unexpected error occurred and has been logged as code 7F3A2C. Please try again."}'), "An unexpected error occurred and has been logged as code 7F3A2C. Please try again."],
    ])("handles update failure: %s", async (_label, error, message) => {
        await open("edit"); fill();
        vi.mocked(apiClient).mockRejectedValueOnce(error);
        await userEvent.click(screen.getByRole("button", {name: "Spara ämne"}));
        expect(await screen.findByRole("alert")).toHaveTextContent(message);
        expect(screen.getByLabelText("Namn (obligatoriskt)")).toHaveValue("Wildlife");
        expect(screen.getByLabelText("Beskrivning (obligatoriskt)")).toHaveValue("Wildlife description");
        expect(screen.getByRole("button", {name: "Spara ämne"})).toBeEnabled();
    });
    it("requires named confirmation, cancels without a request and deletes only on success", async () => {
        await open("delete");
        expect(screen.getByRole("region", {name: /Ta bort ämne|Delete subject/i})).toBeInTheDocument();
        expect(screen.getByRole("button", {name: /Avbryt borttagning|Cancel/i})).toHaveFocus();
        expect(mutations()).toHaveLength(0);
        await userEvent.click(screen.getByRole("button", {name: /Avbryt borttagning|Cancel/i}));
        expect(mutations()).toHaveLength(0);
        expect(screen.getByRole("button", {name: "Ta bort ämne Safety"})).toHaveFocus();
        await userEvent.click(screen.getByRole("button", {name: "Ta bort ämne Safety"}));
        vi.mocked(apiClient).mockResolvedValueOnce(null);
        await userEvent.click(screen.getByRole("button", {name: /Bekräfta borttagning|Confirm/i}));
        expect(apiClient).toHaveBeenCalledWith("/api/admin/subjects/1", {method: "DELETE"});
        expect(await screen.findByRole("status")).toHaveTextContent('Ämne "Safety" har tagits bort.');
        expect(screen.queryByRole("heading", {name: "Safety"})).not.toBeInTheDocument();
        expect(screen.getByRole("button", {name: "Skapa ämne"})).toHaveFocus();
    });
    it("preserves the subject when deletion is refused with 409 (subject in use)", async () => {
        await open("delete");
        vi.mocked(apiClient).mockRejectedValueOnce(httpError(409, '{"message":"This change conflicts with existing data"}'));
        await userEvent.click(screen.getByRole("button", {name: /Bekräfta borttagning|Confirm/i}));
        expect(await screen.findByRole("alert")).toHaveTextContent("This subject cannot currently be deleted because it is in use by questions or practice results. No subject was deleted.");
        expect(screen.getByRole("heading", {name: "Safety"})).toBeInTheDocument();
        expect(screen.queryByRole("status")).not.toBeInTheDocument();
    });
    it.each(["create", "edit", "delete"] as const)("prevents duplicate %s requests and keeps retry usable", async mode => {
        await open(mode);
        if (mode !== "delete") fill();
        let reject!: (error: Error) => void;
        vi.mocked(apiClient).mockImplementationOnce(() => new Promise((_resolve, rejectRequest) => { reject = rejectRequest; }));
        const button = screen.getByRole("button", {name: mode === "delete" ? /Bekräfta borttagning|Confirm/i : "Spara ämne"});
        fireEvent.click(button); fireEvent.click(button);
        if (mode !== "delete") fireEvent.submit(screen.getByRole("form"));
        expect(mutations()).toHaveLength(1);
        expect(button).toBeDisabled();
        expect(screen.getByRole("heading", {name: "Safety"})).toBeInTheDocument();
        await act(async () => reject(new Error("Server unavailable")));
        expect(screen.getByRole("alert")).toHaveTextContent("The request failed. Please try again. If it persists, contact an administrator.");
        expect(button).toBeEnabled();
        vi.mocked(apiClient).mockResolvedValueOnce(mode === "delete" ? null : {...subject, name: "Wildlife"});
        await userEvent.click(button);
        expect(mutations()).toHaveLength(2);
        expect(screen.queryByRole("alert")).not.toBeInTheDocument();
    });
});
