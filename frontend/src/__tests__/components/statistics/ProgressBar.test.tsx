// Tests for src/components/statistics/ProgressBar.tsx

import { describe, it, expect, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import ProgressBar from "../../../components/statistics/ProgressBar";

describe("ProgressBar", () => {

    it("shows the subject name", () => {
        // Arrange: draw the component with test data
        render(<ProgressBar subject="Viltvård" percentage={40} />);

        // Assert: the subject name is visible on the page
        expect(screen.getByText("Viltvård")).toBeInTheDocument();
    });

    it("shows the percentage as text", () => {
        // Arrange: draw the component with test data
        render(<ProgressBar subject="Viltvård" percentage={40} />);

        // Assert: the number is shown with a % sign
        expect(screen.getByText("40%")).toBeInTheDocument();
    });

    it("sets the fill width to the percentage", () => {
        // Arrange: draw the component and keep the "container" (its HTML)
        const { container } = render(<ProgressBar subject="Viltvård" percentage={40} />);

        // Find the coloured fill by its CSS class (it has no text to search for)
        const fill = container.querySelector(".progress-fill");

        // Assert: the fill is 40% wide
        expect(fill).toHaveStyle({ width: "40%" });
    });

    it("calls onClick when clicked", async () => {
        // Arrange: a mock function that records every call, passed as the onClick prop
        const onClick = vi.fn();
        render(<ProgressBar subject="Viltvård" percentage={40} onClick={onClick} />);

        // Act: click the button, the way a user would
        await userEvent.click(screen.getByRole("button"));

        // Assert: the click reached our function exactly once
        expect(onClick).toHaveBeenCalledTimes(1);
    });
});
