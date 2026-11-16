// @vitest-environment jsdom
// Tests for src/components/profile/ResultSharingSettings.tsx (#125)

import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import ResultSharingSettings from "../../../components/profile/ResultSharingSettings";
import { getResultSharing, updateResultSharing } from "../../../services/resultSharingService";

vi.mock("../../../services/resultSharingService", () => ({
    getResultSharing: vi.fn(),
    updateResultSharing: vi.fn(),
}));

// One stable t function, like react-i18next's: a new identity per render
// would make the component's load effect run again on every state change.
vi.mock("react-i18next", () => {
    const t = (key: string) => key;
    return { useTranslation: () => ({ t }) };
});

const getResultSharingMock = vi.mocked(getResultSharing);
const updateResultSharingMock = vi.mocked(updateResultSharing);

const privatePreferences = { shareSubjectStatistics: false, shareMockExamResults: false };

describe("ResultSharingSettings", () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it("shows the stored preferences after loading", async () => {
        getResultSharingMock.mockResolvedValue({
            shareSubjectStatistics: true,
            shareMockExamResults: false,
        });

        render(<ResultSharingSettings />);

        expect(await screen.findByLabelText("profile.shareSubjectStatisticsLabel")).toBeChecked();
        expect(screen.getByLabelText("profile.shareMockExamResultsLabel")).not.toBeChecked();
        expect(getResultSharingMock).toHaveBeenCalledTimes(1);
    });

    it("saves the toggled choices and confirms", async () => {
        getResultSharingMock.mockResolvedValue(privatePreferences);
        updateResultSharingMock.mockResolvedValue({
            shareSubjectStatistics: true,
            shareMockExamResults: false,
        });

        render(<ResultSharingSettings />);

        const subjectStatistics = await screen.findByLabelText("profile.shareSubjectStatisticsLabel");
        fireEvent.click(subjectStatistics);
        fireEvent.click(screen.getByText("profile.resultSharingSaveButton"));

        await waitFor(() =>
            expect(updateResultSharingMock).toHaveBeenCalledWith({
                shareSubjectStatistics: true,
                shareMockExamResults: false,
            }),
        );
        expect(await screen.findByText("profile.resultSharingSuccess")).toBeInTheDocument();
        // The saved server response is what stays on screen after a refresh.
        expect(subjectStatistics).toBeChecked();
    });

    it("reports an error when saving fails", async () => {
        getResultSharingMock.mockResolvedValue(privatePreferences);
        updateResultSharingMock.mockRejectedValue(new Error("boom"));

        render(<ResultSharingSettings />);

        fireEvent.click(await screen.findByLabelText("profile.shareMockExamResultsLabel"));
        fireEvent.click(screen.getByText("profile.resultSharingSaveButton"));

        expect(await screen.findByRole("alert")).toHaveTextContent(
            "profile.resultSharingSaveError",
        );
        expect(screen.queryByText("profile.resultSharingSuccess")).not.toBeInTheDocument();
    });

    it("reports an error when the preferences cannot be loaded", async () => {
        getResultSharingMock.mockRejectedValue(new Error("boom"));

        render(<ResultSharingSettings />);

        expect(await screen.findByRole("alert")).toHaveTextContent(
            "profile.resultSharingLoadError",
        );
        expect(screen.queryByLabelText("profile.shareSubjectStatisticsLabel")).toBeNull();
    });
});
