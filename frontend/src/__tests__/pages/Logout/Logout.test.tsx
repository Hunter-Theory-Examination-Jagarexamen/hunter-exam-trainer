// Tests for src/pages/Logout/Logout.tsx

import { describe, it, expect } from "vitest";
import { render, screen } from "@testing-library/react";
import { createMemoryRouter, RouterProvider } from "react-router-dom";
import Logout from "../../../pages/Logout/Logout";

describe("Logout page", () => {

    it("forgets the login and goes to the login page (#188)", async () => {
        localStorage.setItem("token", "test-token");
        const router = createMemoryRouter([
            { path: "/logout", element: <Logout /> },
            { path: "/login", element: <p>Login page</p> },
        ], { initialEntries: ["/dashboard", "/logout"] });
        render(<RouterProvider router={router} />);

        expect(await screen.findByText("Login page")).toBeInTheDocument();
        expect(localStorage.getItem("token")).toBeNull();
        // replace: Back doesn't return to /logout.
        expect(router.state.historyAction).toBe("REPLACE");
    });
});
