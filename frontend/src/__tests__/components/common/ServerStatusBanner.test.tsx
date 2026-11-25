import {afterEach, describe, expect, it, vi} from "vitest";
import {act, render, screen} from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import ServerStatusBanner from "../../../components/common/ServerStatusBanner";
import apiClient from "../../../api/apiClient";
import {setServerStatus} from "../../../api/serverStatus";
import "../../../i18n/config";

afterEach(() => {
    act(() => setServerStatus("ok"));
    vi.unstubAllGlobals();
    vi.useRealTimers();
});

describe("ServerStatusBanner", () => {
    it("shows nothing while the server answers", () => {
        render(<ServerStatusBanner />);
        expect(screen.queryByRole("alert")).not.toBeInTheDocument();
    });

    it("says the server is starting while a request waits", () => {
        render(<ServerStatusBanner />);
        act(() => setServerStatus("starting"));
        expect(screen.getByRole("status")).toHaveTextContent("Servern startar. Det kan ta upp till en minut…");
        expect(screen.queryByRole("alert")).not.toBeInTheDocument();
    });

    it("shows the starting banner when a request gets no answer, and hides it when the next one does", async () => {
        render(<ServerStatusBanner />);

        vi.stubGlobal("fetch", vi.fn().mockRejectedValue(new TypeError("Failed to fetch")));
        await act(() => apiClient("/api/users/me").catch(() => {}));
        expect(screen.getByRole("status")).toHaveTextContent("Servern startar");
        expect(screen.queryByRole("alert")).not.toBeInTheDocument();

        vi.stubGlobal("fetch", vi.fn().mockResolvedValue(new Response(null, {status: 204})));
        await act(() => apiClient("/api/users/me"));
        expect(screen.queryByRole("status")).not.toBeInTheDocument();
    });

    it("a cold start that recovers never ends with the red banner (#206)", async () => {
       vi.useFakeTimers();
       render(<ServerStatusBanner />);

       // The server is waking up: the request fails.
        vi.stubGlobal("fetch", vi.fn().mockRejectedValue(new TypeError("Failed to fetch")));
        await act(() => apiClient("/api/users/me").catch(() => {}));
        expect(screen.getByRole("status")).toBeInTheDocument();

        // Still waking up after 30 s: the banner stays orange.
        await act(() => vi.advanceTimersByTimeAsync(30_000));
        expect(screen.getByRole("status")).toBeInTheDocument();
        expect(screen.queryByRole("alert")).not.toBeInTheDocument();

        // The server answers: the next background check hides the banner.
        vi.stubGlobal("fetch", vi.fn().mockResolvedValue(new Response(null, {status: 200})));
        await act(() => vi.advanceTimersByTimeAsync(3_000));
        expect(screen.queryByRole("status")).not.toBeInTheDocument();
        expect(screen.queryByRole("alert")).not.toBeInTheDocument();
    });

    it("'Try again' says so when the server still can't be reached (#188)", async () => {
        render(<ServerStatusBanner />);
        act(() => setServerStatus("unreachable"));
        vi.stubGlobal("fetch", vi.fn().mockRejectedValue(new TypeError("Failed to fetch")));

        await userEvent.click(screen.getByRole("button", {name: /försök igen/i}));

        expect(await screen.findByText(/Fortfarande ingen kontakt/)).toBeInTheDocument();
    });

    it("'Try again' on the exam page hides the banner without reloading (#188)", async () => {
        window.history.pushState({}, "", "/exam");
        render(<ServerStatusBanner />);
        act(() => setServerStatus("unreachable"));
        vi.stubGlobal("fetch", vi.fn().mockResolvedValue(new Response(null, {status: 200})));

        await userEvent.click(screen.getByRole("button", {name: /försök igen/i}));

        expect(screen.queryByRole("alert")).not.toBeInTheDocument();
        window.history.pushState({}, "", "/");
    });

    it("a failed request while the banner is red keeps it red (#254)", async () => {
        render(<ServerStatusBanner />);
        act(() => setServerStatus("unreachable"));
        vi.stubGlobal("fetch", vi.fn().mockRejectedValue(new TypeError("Failed to fetch")));

        await act(() => apiClient("/api/users/me").catch(() => {}));

        expect(screen.getByRole("alert")).toBeInTheDocument();
        expect(screen.getByRole("button", {name: /försök igen/i})).toBeInTheDocument();
        expect(screen.queryByRole("status")).not.toBeInTheDocument();
    });
});
