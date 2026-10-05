import {afterEach, beforeEach, describe, expect, it, vi} from "vitest";
import {render, screen, waitFor} from "@testing-library/react";
import AppRoutes from "../../routes/AppRoutes";
import apiClient from "../../api/apiClient";
import {UserRole} from "../../types/user";

vi.mock("../../api/apiClient", () => ({default: vi.fn()}));
vi.mock("../../services/authService", () => ({
    checkGoogleLoginEnabled: async () => false,
    loginUser: vi.fn(),
}));
vi.mock("../../pages/Dashboard/Dashboard", () => ({default: () => <h1>Dashboard page</h1>}));

beforeEach(() => {
    vi.resetAllMocks();
    localStorage.setItem("token", "test-token");
    window.history.replaceState({}, "", "/admin/questions");
});
afterEach(() => localStorage.clear());

function mockUser(role: typeof UserRole[keyof typeof UserRole]) {
    vi.mocked(apiClient).mockImplementation(async endpoint => {
        if (endpoint === "/api/users/me") return {id: 1, fullName: "Test User", email: "test@example.com", role, createdAt: "2026-10-05"};
        if (endpoint === "/api/subjects") return [];
        throw new Error(`Unexpected endpoint: ${endpoint}`);
    });
}

describe("Admin navigation and routing", () => {
    it("lets an ADMIN access the route and shows admin navigation", async () => {
        mockUser(UserRole.ADMIN);
        render(<AppRoutes />);
        expect(screen.getByText("Loading user details...")).toBeInTheDocument();
        expect(screen.queryByRole("link", {name: "Admin"})).not.toBeInTheDocument();
        expect(await screen.findByRole("heading", {name: "Admin Question Bank"})).toBeInTheDocument();
        expect(screen.getByRole("link", {name: "Admin"})).toHaveAttribute("href", "/admin/questions");
        expect(screen.getByText("Welcome, Test User")).toBeInTheDocument();
        expect(vi.mocked(apiClient).mock.calls.filter(([endpoint]) => endpoint === "/api/users/me")).toHaveLength(1);
    });

    it("redirects a STUDENT opening the URL directly and hides admin navigation", async () => {
        mockUser(UserRole.STUDENT);
        render(<AppRoutes />);
        expect(await screen.findByRole("heading", {name: "Dashboard page"})).toBeInTheDocument();
        expect(window.location.pathname).toBe("/dashboard");
        expect(screen.queryByRole("link", {name: "Admin"})).not.toBeInTheDocument();
        expect(apiClient).not.toHaveBeenCalledWith("/api/subjects");
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
        expect(await screen.findByRole("alert")).toHaveTextContent("Failed to load user details.");
        expect(screen.queryByRole("link", {name: "Admin"})).not.toBeInTheDocument();
        expect(apiClient).not.toHaveBeenCalledWith("/api/subjects");
    });
});
