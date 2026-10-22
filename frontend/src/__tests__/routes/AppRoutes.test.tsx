import { render, screen } from "@testing-library/react";
import {describe, it, expect, beforeEach, afterEach} from "vitest";
import AppRoutes from "../../routes/AppRoutes";

beforeEach(() => {
    window.history.replaceState({}, "", "/some-unknown-page-123");
});
afterEach(() => {
    window.history.replaceState({}, "", "/");
});

describe("AppRoutes - Unknown routes", () => {
    it("renders the 404 page when navigating to an unknown address", () => {
        render(<AppRoutes />);
        expect(screen.getByText(/404 - Page Not Found/i)).toBeInTheDocument();
        expect(window.location.pathname).toBe("/some-unknown-page-123");
    });
});