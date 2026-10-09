import {afterEach, beforeEach, describe, expect, it, vi} from "vitest";
import {render, screen, within} from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import AppRoutes from "../../../routes/AppRoutes";
import apiClient from "../../../api/apiClient";
import {UserRole} from "../../../types/user";
import type {Subject} from "../../../types/subject";

vi.mock("../../../api/apiClient", () => ({default: vi.fn()}));
beforeEach(() => {
    vi.resetAllMocks(); localStorage.setItem("token", "test-token");
    window.history.replaceState({}, "", "/admin/subjects");
});
afterEach(() => localStorage.clear());

describe("Subject synchronization on navigation", () => {
    it.each(["create", "edit", "delete"] as const)("refreshes question selectors and practice cards after %s", async mode => {
        let subjects: Subject[] = [{id: 1, name: "Safety", description: "Safe hunting", questionCount: 0},
            {id: 2, name: "Wildlife", description: "Animals", questionCount: 0}];
        vi.mocked(apiClient).mockImplementation(async (endpoint, options) => {
            if (endpoint === "/api/users/me") return {id: 1, fullName: "Admin", role: UserRole.ADMIN};
            if (options?.method === "POST" || options?.method === "PUT") {
                const saved = {...JSON.parse(options.body as string), id: options.method === "POST" ? 3 : 1, questionCount: 0};
                subjects = options.method === "POST" ? [...subjects, saved] : subjects.map(item => item.id === 1 ? saved : item);
                return saved;
            }
            if (options?.method === "DELETE") { subjects = subjects.filter(item => item.id !== 1); return null; }
            if (endpoint === "/api/subjects") return subjects;
            if (endpoint.startsWith("/api/questions?")) return [];
            throw new Error(`Unexpected endpoint ${endpoint}`);
        });
        render(<AppRoutes />);
        await screen.findByRole("heading", {name: "Safety"}, {timeout: 5000});
        await userEvent.click(screen.getByRole("button", {name: mode === "create" ? "Create subject" : `${mode === "edit" ? "Edit" : "Delete"} subject Safety`}));
        if (mode === "delete") await userEvent.click(screen.getByRole("button", {name: "Confirm deletion"}));
        else {
            await userEvent.clear(screen.getByLabelText("Name (required)"));
            await userEvent.type(screen.getByLabelText("Name (required)"), "New subject");
            await userEvent.clear(screen.getByLabelText("Description (required)"));
            await userEvent.type(screen.getByLabelText("Description (required)"), "New description");
            await userEvent.click(screen.getByRole("button", {name: "Save subject"}));
        }
        await screen.findByText(new RegExp(`Subject .* ${mode === "create" ? "created" : mode === "edit" ? "updated" : "deleted"}\\.`));
        await userEvent.click(screen.getByRole("link", {name: "Admin"}));
        await screen.findByRole("option", {name: `${mode === "delete" ? "Wildlife" : "New subject"} (0)`});
        if (mode !== "create") expect(screen.queryByRole("option", {name: "Safety (0)"})).not.toBeInTheDocument();
        await userEvent.click(screen.getByRole("button", {name: "Add question"}));
        expect(screen.getByLabelText("Question subject (required)")).toHaveTextContent(mode === "delete" ? "Wildlife" : "New subject");
        await userEvent.click(screen.getByRole("link", {name: "Practice"}));
        const practiceCard = (await screen.findByRole("heading", {name: "Practice by Subject"})).closest(".practice-card")!;
        await userEvent.click(within(practiceCard as HTMLElement).getByRole("button", {name: "Select"}));
        expect(await screen.findByRole("heading", {name: mode === "delete" ? "Wildlife" : "New subject"})).toBeInTheDocument();
        if (mode !== "delete") expect(screen.getByText("New description")).toBeInTheDocument();
        if (mode !== "create") expect(screen.queryByRole("heading", {name: "Safety"})).not.toBeInTheDocument();
        expect(vi.mocked(apiClient).mock.calls.filter(([endpoint, options]) => endpoint === "/api/subjects" && !options?.method)).toHaveLength(3);
    });
});
