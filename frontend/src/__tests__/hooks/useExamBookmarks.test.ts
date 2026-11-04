import { describe, it, expect, beforeEach, afterEach } from "vitest";
import { renderHook, act } from "@testing-library/react";
import { useExamBookmarks } from "../../hooks/useExamBookmarks";

describe("useExamBookmarks", () => {

    beforeEach(() => {
        localStorage.clear();
    });

    afterEach(() => {
        localStorage.clear();
    });

    it("starts empty", () => {
        const { result } = renderHook(() => useExamBookmarks("attempt-1"));
        expect(result.current.bookmarks.size).toBe(0);
    });

    it("toggles a bookmark on and off", () => {
        const { result } = renderHook(() => useExamBookmarks("attempt-1"));

        act(() => result.current.toggleBookmark(5));
        expect(result.current.isBookmarked(5)).toBe(true);

        act(() => result.current.toggleBookmark(5));
        expect(result.current.isBookmarked(5)).toBe(false);
    });

    it("persists bookmarks in localStorage", () => {
        const { result } = renderHook(() => useExamBookmarks("attempt-1"));
        act(() => {
            result.current.toggleBookmark(5);
            result.current.toggleBookmark(10);
        });

        const stored = localStorage.getItem("exam-bookmarks-attempt-1");
        expect(stored).toBeTruthy();
        const parsed = JSON.parse(stored!);
        expect(parsed).toEqual(expect.arrayContaining([5, 10]));
    });

    it("keeps bookmarks separate between attempts", () => {
        const { result: r1 } = renderHook(() => useExamBookmarks("attempt-1"));
        const { result: r2 } = renderHook(() => useExamBookmarks("attempt-2"));

        act(() => r1.current.toggleBookmark(5));

        expect(r1.current.isBookmarked(5)).toBe(true);
        expect(r2.current.isBookmarked(5)).toBe(false);
    });

    it("does nothing while attemptId is null", () => {
        const { result } = renderHook(() => useExamBookmarks(null));

        act(() => result.current.toggleBookmark(5));

        // Bookmark exists in state but isn't persisted
        expect(localStorage.getItem("exam-bookmarks-null")).toBeNull();
    });
});
