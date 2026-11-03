import { render, screen } from "@testing-library/react";
import {describe, it, expect} from "vitest";
import {createMemoryRouter, RouterProvider} from "react-router-dom";
import {routes} from "../../routes/routes";

// Uses the app's real routes with a memory router, so the test can start at any
// address without changing the browser's location.
describe("AppRoutes - Unknown routes", () => {
    it("renders the 404 page when navigating to an unknown address", () => {
        const router = createMemoryRouter(routes, {initialEntries: ["/some-unknown-page-123"]});
        render(<RouterProvider router={router} />);
        expect(screen.getByText(/404 - Page Not Found/i)).toBeInTheDocument();
        expect(router.state.location.pathname).toBe("/some-unknown-page-123");
    });
});
