import {afterEach, beforeEach, describe, expect, it, vi} from "vitest";
import {render, screen, waitFor} from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import AppRoutes from "../../routes/AppRoutes";
import apiClient from "../../api/apiClient";
import {UserRole} from "../../types/user";
import {fakeToken} from "../fakeToken";

vi.mock("../../api/apiClient", () => ({default: vi.fn()}));
vi.mock("../../services/authService", () => ({
    checkGoogleLoginEnabled: async () => false,
    loginUser: vi.fn(),
}));
vi.mock("../../pages/Dashboard/Dashboard", () => ({default: () => <h1>Dashboard page</h1>}));

beforeEach(() => {
    vi.resetAllMocks();
    localStorage.setItem("token", fakeToken());
    window.history.replaceState({}, "", "/admin/questions");
});
afterEach(() => localStorage.clear());

function mockUser(role: typeof UserRole[keyof typeof UserRole]) {
    vi.mocked(apiClient).mockImplementation(async endpoint => {
        if (endpoint === "/api/users/me") return {id: 1, fullName: "Test User", email: "test@example.com", role, createdAt: "2026-10-05"};
        if (endpoint === "/api/subjects") return [];
        if (endpoint === "/api/admin/stats") return {questionCount: 42, subjectCount: 5, studentCount: 17};
        throw new Error(`Unexpected endpoint: ${endpoint}`);
    });
}

describe("Admin navigation and routing", () => {
    it("lets ADMIN navigate to subject management", async () => {
        mockUser(UserRole.ADMIN);
        render(<AppRoutes />);
        await userEvent.click(await screen.findByRole("link", {name: /Ämnen|Subjects/i}));
        expect(await screen.findByRole("heading", {name: /Hantera ämnen|Subjects/i})).toBeInTheDocument();
        expect(window.location.pathname).toBe("/admin/subjects");
    });

    it("blocks a STUDENT opening subject management directly and hides its link", async () => {
        window.history.replaceState({}, "", "/admin/subjects");
        mockUser(UserRole.STUDENT);
        render(<AppRoutes />);
        expect(await screen.findByRole("heading", {name: "Dashboard page"})).toBeInTheDocument();
        expect(screen.queryByRole("link", {name: /Ämnen|Subjects/i})).not.toBeInTheDocument();
        expect(apiClient).not.toHaveBeenCalledWith("/api/subjects");
        expect(window.location.pathname).toBe("/dashboard");
    });

    it("lets an ADMIN open question creation through the protected route", async () => {
        vi.mocked(apiClient).mockImplementation(async endpoint => {
            if (endpoint === "/api/users/me") return {id: 1, fullName: "Admin", role: UserRole.ADMIN};
            if (endpoint === "/api/subjects") return [{id: 1, name: "Safety", description: "", questionCount: 0}];
            return [];
        });
        render(<AppRoutes />);
        await userEvent.click(await screen.findByRole("button", {name: "Lägg till fråga"}, {timeout: 5000}));
        expect(screen.getByRole("form", {name: "Lägg till fråga"})).toBeInTheDocument();
    });

    it("lets an ADMIN access the route and shows admin navigation", async () => {
        mockUser(UserRole.ADMIN);
        render(<AppRoutes />);
        expect(screen.getByText(/laddar|loading/i)).toBeInTheDocument();
        expect(screen.queryByRole("link", {name: /^Admin$/i})).not.toBeInTheDocument();
        expect(await screen.findByRole("heading", {name: /Admin|Frågebank/i})).toBeInTheDocument();
        expect(screen.getByRole("link", {name: /^Admin$/i})).toHaveAttribute("href", "/admin");
        expect(screen.getByRole("link", {name: /Frågor|Questions/i})).toHaveAttribute("href", "/admin/questions");
        expect(screen.getByRole("link", {name: /Ämnen|Subjects/i})).toHaveAttribute("href", "/admin/subjects");
        expect(screen.getByRole("link", {name: /Deltagare|Participants/i})).toHaveAttribute("href", "/admin/learners");
        expect(screen.getByText("Välkommen, Test User")).toBeInTheDocument();
        expect(vi.mocked(apiClient).mock.calls.filter(([endpoint]) => endpoint === "/api/users/me")).toHaveLength(1);
    });

    it("redirects a STUDENT opening the URL directly and hides admin navigation", async () => {
        mockUser(UserRole.STUDENT);
        render(<AppRoutes />);
        expect(await screen.findByRole("heading", {name: "Dashboard page"})).toBeInTheDocument();
        expect(window.location.pathname).toBe("/dashboard");
        expect(screen.queryByRole("link", {name: /^Admin$/i})).not.toBeInTheDocument();
        expect(apiClient).not.toHaveBeenCalledWith("/api/subjects");
        expect(screen.queryByRole("button", {name: /Skapa fråga|Create/i})).not.toBeInTheDocument();
    });

    it("lets an ADMIN open the admin start page with count cards linking to each tool", async () => {
        window.history.replaceState({}, "", "/admin");
        mockUser(UserRole.ADMIN);
        render(<AppRoutes />);
        expect(await screen.findByRole("heading", {name: /Admin/i})).toBeInTheDocument();
        const questionsCard = await screen.findByRole("link", {name: /Öppna frågebanken/i});
        expect(questionsCard).toHaveAttribute("href", "/admin/questions");
        expect(questionsCard).toHaveTextContent(/Frågor/i);
        expect(screen.getAllByRole("link", {name: /Frågor|Questions/i}).length).toBeGreaterThan(0);
        expect(screen.getAllByRole("link", {name: /Ämnen|Subjects/i}).length).toBeGreaterThan(0);
        expect(screen.getAllByRole("link", {name: /Deltagare|Participants/i}).length).toBeGreaterThan(0);
    });

    it("redirects a STUDENT opening the admin start page without requesting the counts", async () => {
        window.history.replaceState({}, "", "/admin");
        mockUser(UserRole.STUDENT);
        render(<AppRoutes />);
        expect(await screen.findByRole("heading", {name: "Dashboard page"})).toBeInTheDocument();
        expect(window.location.pathname).toBe("/dashboard");
        expect(screen.queryByRole("link", {name: /^Admin$/i})).not.toBeInTheDocument();
        expect(apiClient).not.toHaveBeenCalledWith("/api/admin/stats");
    });

    it("redirects unauthenticated access to login without requesting admin data", async () => {
        localStorage.clear();
        render(<AppRoutes />);
        await waitFor(() => expect(window.location.pathname).toBe("/login"));
        expect(apiClient).not.toHaveBeenCalled();
    });

    it("fails closed when user details cannot be loaded", async () => {
        vi.mocked(apiClient).mockRejectedValue(new Error("Server down"));
        render(<AppRoutes />);
        expect(await screen.findByRole("alert")).toHaveTextContent(/Kunde inte ladda|Could not load/i);
        expect(screen.queryByRole("link", {name: /^Admin$/i})).not.toBeInTheDocument();
        expect(apiClient).not.toHaveBeenCalledWith("/api/subjects");
    });
});
