// Tests for the feedback entries in src/components/layout/Sidebar.tsx.

import { beforeEach, describe, expect, it, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import Sidebar from "../../../components/layout/Sidebar";
import { UserRole, type User } from "../../../types/user";

vi.mock("../../../api/apiClient", () => ({ default: vi.fn() }));

const student: User = {
    id: 1, fullName: "Test Student", email: "student@example.test",
    role: UserRole.STUDENT, createdAt: "2026-11-01T10:00:00",
};
const admin: User = { ...student, id: 2, fullName: "Test Admin", role: UserRole.ADMIN };

const renderSidebar = (user: User, onClose = vi.fn()) =>
    render(<MemoryRouter><Sidebar user={user} isOpen={true} onClose={onClose} /></MemoryRouter>);

beforeEach(() => vi.resetAllMocks());

describe("Sidebar feedback entries", () => {
    it("lets a student open the feedback form from the menu", async () => {
        const onClose = vi.fn();
        renderSidebar(student, onClose);

        await userEvent.click(screen.getByRole("button", { name: "Ge feedback" }));

        expect(screen.getByRole("dialog", { name: "Ge feedback" })).toBeInTheDocument();
        expect(screen.getByLabelText("Vad gäller det?")).toHaveValue("APP");
        expect(onClose).toHaveBeenCalled();   // the mobile menu closes
    });

    it("shows the admin feedback link only to admins", () => {
        renderSidebar(student);
        expect(screen.queryByRole("link", { name: "Feedback" })).not.toBeInTheDocument();
    });

    it("links admins to the feedback page", () => {
        renderSidebar(admin);
        expect(screen.getByRole("link", { name: "Feedback" })).toHaveAttribute("href", "/admin/feedback");
    });
});
