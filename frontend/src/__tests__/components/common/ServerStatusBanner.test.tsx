import {afterEach, describe, expect, it, vi} from "vitest";
import {act, render, screen} from "@testing-library/react";
import ServerStatusBanner from "../../../components/common/ServerStatusBanner";
import apiClient from "../../../api/apiClient";
import {setServerStatus} from "../../../api/serverStatus";

afterEach(() => {
    act(() => setServerStatus("ok"));
    vi.unstubAllGlobals();
});

describe("ServerStatusBanner", () => {
    it("shows nothing while the server answers", () => {
        render(<ServerStatusBanner />);
        expect(screen.queryByRole("alert")).not.toBeInTheDocument();
    });

    it("says the server is starting while a request waits", () => {
        render(<ServerStatusBanner />);
        act(() => setServerStatus("starting"));
        expect(screen.getByRole("status")).toHaveTextContent("The server is starting");
        expect(screen.queryByRole("alert")).not.toBeInTheDocument();
    });

    it("appears when a request gets no answer, and disappears when the next one does", async () => {
        render(<ServerStatusBanner />);

        vi.stubGlobal("fetch", vi.fn().mockRejectedValue(new TypeError("Failed to fetch")));
        await act(() => apiClient("/api/users/me").catch(() => {}));
        expect(screen.getByRole("alert")).toHaveTextContent("Can't reach the server");
        expect(screen.getByRole("button", {name: "Try again"})).toBeInTheDocument();

        vi.stubGlobal("fetch", vi.fn().mockResolvedValue(new Response(null, {status: 204})));
        await act(() => apiClient("/api/users/me"));
        expect(screen.queryByRole("alert")).not.toBeInTheDocument();
    });
});
