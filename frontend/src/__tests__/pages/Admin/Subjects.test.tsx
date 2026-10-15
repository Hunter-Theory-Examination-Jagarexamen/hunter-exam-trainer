import {beforeEach, describe, expect, it, vi} from "vitest";
import {act, fireEvent, render, screen} from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import Subjects from "../../../pages/Admin/Subjects";
import apiClient from "../../../api/apiClient";

vi.mock("../../../api/apiClient", () => ({default: vi.fn()}));
const subject = {id: 1, name: "Safety", description: "Safe hunting", questionCount: 0};
const mutations = () => vi.mocked(apiClient).mock.calls.filter(([, options]) => options?.method);
beforeEach(() => { vi.resetAllMocks(); vi.mocked(apiClient).mockResolvedValue([subject]); });

async function open(mode: "create" | "edit" | "delete") {
    render(<Subjects />);
    await screen.findByRole("heading", {name: "Safety"});
    await userEvent.click(screen.getByRole("button", {name: mode === "create" ? "Create subject" : `${mode === "edit" ? "Edit" : "Delete"} subject Safety`}));
}
function fill(name = "Wildlife", description = "Wildlife description") {
    fireEvent.change(screen.getByLabelText("Name (required)"), {target: {value: name}});
    fireEvent.change(screen.getByLabelText("Description (required)"), {target: {value: description}});
}

describe("Subject management", () => {
    it("loads names, descriptions and question counts", async () => {
        render(<Subjects />);
        expect(screen.getByRole("status")).toHaveTextContent("Loading subjects...");
        expect(await screen.findByRole("heading", {name: "Safety"})).toBeInTheDocument();
        expect(screen.getByText("Safe hunting")).toBeInTheDocument();
        expect(screen.getByText("0 questions")).toBeInTheDocument();
        expect(apiClient).toHaveBeenCalledWith("/api/subjects");
    });
    it("shows an empty state and allows creation", async () => {
        vi.mocked(apiClient).mockResolvedValue([]);
        render(<Subjects />);
        expect(await screen.findByText(/No subjects available/)).toBeInTheDocument();
        await userEvent.click(screen.getByRole("button", {name: "Create subject"}));
        expect(screen.getByRole("form", {name: "Create subject"})).toBeInTheDocument();
        expect(screen.getByLabelText("Name (required)")).toHaveFocus();
    });
    it("shows a safe load failure and retries", async () => {
        vi.mocked(apiClient).mockRejectedValueOnce(new Error("SQL secret"));
        render(<Subjects />);
        expect(await screen.findByRole("alert")).toHaveTextContent("Failed to load subjects.");
        await userEvent.click(screen.getByRole("button", {name: "Retry loading subjects"}));
        expect(await screen.findByRole("heading", {name: "Safety"})).toBeInTheDocument();
    });
    it.each([["", "description", "Enter a subject name."], ["   ", "description", "Enter a subject name."],
        ["Name", "   ", "Enter a subject description."], ["x".repeat(256), "description", "at most 255 characters"],
        ["Name", "x".repeat(256), "at most 255 characters"]])("validates input before sending", async (name, description, message) => {
        await open("create"); fill(name, description);
        await userEvent.click(screen.getByRole("button", {name: "Save subject"}));
        expect(screen.getByRole("alert")).toHaveTextContent(message);
        expect(mutations()).toHaveLength(0);
    });
    it("creates using the required payload and immediately displays the server response", async () => {
        await open("create"); fill(" Wildlife ", " Wildlife description ");
        vi.mocked(apiClient).mockResolvedValueOnce({...subject, id: 2, name: "Wildlife", description: "Wildlife description"});
        await userEvent.click(screen.getByRole("button", {name: "Save subject"}));
        expect(apiClient).toHaveBeenCalledWith("/api/subjects", {method: "POST", body: JSON.stringify({name: "Wildlife", description: "Wildlife description"})});
        expect(await screen.findByRole("heading", {name: "Wildlife"})).toBeInTheDocument();
        expect(screen.getByRole("status")).toHaveTextContent('Subject "Wildlife" created.');
        expect(screen.getByRole("button", {name: "Create subject"})).toHaveFocus();
    });
    it("prepopulates and updates both fields using PUT", async () => {
        await open("edit");
        expect(screen.getByLabelText("Name (required)")).toHaveValue("Safety");
        expect(screen.getByLabelText("Description (required)")).toHaveValue("Safe hunting");
        fill("Renamed", "New description");
        vi.mocked(apiClient).mockResolvedValueOnce({...subject, name: "Renamed", description: "New description"});
        await userEvent.click(screen.getByRole("button", {name: "Save subject"}));
        expect(apiClient).toHaveBeenCalledWith("/api/subjects/1", {method: "PUT", body: JSON.stringify({name: "Renamed", description: "New description"})});
        expect(await screen.findByRole("heading", {name: "Renamed"})).toBeInTheDocument();
        expect(screen.getByText("New description")).toBeInTheDocument();
        expect(screen.queryByRole("heading", {name: "Safety"})).not.toBeInTheDocument();
        expect(screen.getByRole("status")).toHaveTextContent("updated");
    });
    it.each(["create", "edit"] as const)("translates real duplicate-key failures on %s and preserves inputs", async mode => {
        await open(mode); fill();
        vi.mocked(apiClient).mockRejectedValueOnce(new Error(JSON.stringify({message: "could not execute statement [Duplicate entry 'Wildlife' for key 'subject.UK_abc'] [insert into subject]"})));
        await userEvent.click(screen.getByRole("button", {name: "Save subject"}));
        expect(await screen.findByRole("alert")).toHaveTextContent("A subject with this name already exists. Choose a different name.");
        expect(screen.getByLabelText("Name (required)")).toHaveValue("Wildlife");
        expect(screen.getByRole("alert")).not.toHaveTextContent("UK_abc");
    });
    it.each([['{"message":"404 NOT_FOUND Subject not found with id: 1"}', "This subject no longer exists"],
        ['{"message":"400 BAD_REQUEST Subject not found with id: 1"}', "This subject no longer exists"],
        ["API request failed: 403", "You do not have permission"], ["Failed to fetch", "Please try again"],
        ['{"message":"Description is required"}', "Enter a subject description"]])("handles update failure %s", async (error, message) => {
        await open("edit"); fill();
        vi.mocked(apiClient).mockRejectedValueOnce(new Error(error));
        await userEvent.click(screen.getByRole("button", {name: "Save subject"}));
        expect(await screen.findByRole("alert")).toHaveTextContent(message);
        expect(screen.getByLabelText("Name (required)")).toHaveValue("Wildlife");
        expect(screen.getByLabelText("Description (required)")).toHaveValue("Wildlife description");
        expect(screen.getByRole("button", {name: "Save subject"})).toBeEnabled();
    });
    it("requires named confirmation, cancels without a request and deletes only on success", async () => {
        await open("delete");
        expect(screen.getByRole("region", {name: 'Delete subject "Safety"?'})).toBeInTheDocument();
        expect(screen.getByRole("button", {name: "Cancel deletion"})).toHaveFocus();
        expect(mutations()).toHaveLength(0);
        await userEvent.click(screen.getByRole("button", {name: "Cancel deletion"}));
        expect(mutations()).toHaveLength(0);
        expect(screen.getByRole("button", {name: "Delete subject Safety"})).toHaveFocus();
        await userEvent.click(screen.getByRole("button", {name: "Delete subject Safety"}));
        vi.mocked(apiClient).mockResolvedValueOnce(null);
        await userEvent.click(screen.getByRole("button", {name: "Confirm deletion"}));
        expect(apiClient).toHaveBeenCalledWith("/api/subjects/1", {method: "DELETE"});
        expect(await screen.findByRole("status")).toHaveTextContent('Subject "Safety" deleted.');
        expect(screen.queryByRole("heading", {name: "Safety"})).not.toBeInTheDocument();
        expect(screen.getByRole("button", {name: "Create subject"})).toHaveFocus();
    });
    it.each(["questions", "practice_result"])("preserves subjects on %s foreign-key failures and hides technical details", async table => {
        await open("delete");
        vi.mocked(apiClient).mockRejectedValueOnce(new Error(JSON.stringify({message: `could not execute statement [Cannot delete or update a parent row: a foreign key constraint fails (${table}, CONSTRAINT FK_secret)] [delete from subject where id=?]`})));
        await userEvent.click(screen.getByRole("button", {name: "Confirm deletion"}));
        expect(await screen.findByRole("alert")).toHaveTextContent("in use by questions or practice results");
        expect(screen.getByRole("alert")).not.toHaveTextContent("FK_secret");
        expect(screen.getByRole("heading", {name: "Safety"})).toBeInTheDocument();
        expect(screen.queryByRole("status")).not.toBeInTheDocument();
    });
    it.each(["create", "edit", "delete"] as const)("prevents duplicate %s requests and keeps retry usable", async mode => {
        await open(mode);
        if (mode !== "delete") fill();
        let reject!: (error: Error) => void;
        vi.mocked(apiClient).mockImplementationOnce(() => new Promise((_resolve, rejectRequest) => { reject = rejectRequest; }));
        const button = screen.getByRole("button", {name: mode === "delete" ? "Confirm deletion" : "Save subject"});
        fireEvent.click(button); fireEvent.click(button);
        if (mode !== "delete") fireEvent.submit(screen.getByRole("form"));
        expect(mutations()).toHaveLength(1);
        expect(button).toBeDisabled();
        expect(screen.getByRole("heading", {name: "Safety"})).toBeInTheDocument();
        await act(async () => reject(new Error("Server unavailable")));
        expect(screen.getByRole("alert")).toHaveTextContent("Please try again");
        expect(button).toBeEnabled();
        vi.mocked(apiClient).mockResolvedValueOnce(mode === "delete" ? null : {...subject, name: "Wildlife"});
        await userEvent.click(button);
        expect(mutations()).toHaveLength(2);
        expect(screen.queryByRole("alert")).not.toBeInTheDocument();
    });
});
