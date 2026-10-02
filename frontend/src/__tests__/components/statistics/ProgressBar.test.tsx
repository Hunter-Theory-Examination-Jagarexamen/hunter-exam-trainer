// Tests for src/components/statistics/ProgressBar.tsx

import { describe, it, expect } from "vitest";
import { render, screen } from "@testing-library/react";
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

    // it("sets the fill width to the percentage", () => {
    //     // Arrange / Act / Assert: the .progress-fill element has width 40%
    // });
    //
    // it("calls onClick when clicked", () => {
    //     // Arrange: a mock function, onClick = vi.fn(), passed as a prop
    //     // Act:     click the button
    //     // Assert:  onClick was called once
    // });
});
