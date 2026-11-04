import { useCallback, useEffect, useState } from "react";

const STORAGE_KEY_PREFIX = "exam-bookmarks-";

/**
 * Persists per-exam bookmarks in localStorage.
 *
 * <p>
 * Bookmarks are scoped per attempt (attemptId). They survive a page
 * reload but are cleared when a new exam starts. They are NOT sent to
 * the server they only affect the UI (jumping to a question).
 *
 * @param attemptId the current exam attempt, or null while loading
 * @returns the current bookmark set and helpers to toggle / query
 */

export const useExamBookmarks = (attemptId: string | null) => {

    const storageKey = attemptId ? `${STORAGE_KEY_PREFIX}${attemptId}` : null;

    const [bookmarks, setBookmarks] = useState<Set<number>>(() => {
        if (!storageKey) return new Set();
        try {
            const raw = localStorage.getItem(storageKey);
            if (!raw) return new Set();
            const parsed = JSON.parse(raw);
            if (Array.isArray(parsed)) {
                return new Set(parsed.filter((n): n is number => typeof n === "number"));
            }
        } catch (err) {
            console.warn("Failed to load exam bookmarks:", err);
        }
        return new Set();
    });

    // Persist on change
    useEffect(() => {
        if (!storageKey) return;
        try {
            localStorage.setItem(storageKey, JSON.stringify([...bookmarks]));
        } catch (err) {
            console.warn("Failed to save exam bookmarks:", err);
        }
    }, [bookmarks, storageKey]);

    // Load when attemptId becomes available (page started with null)
    useEffect(() => {
        if (!storageKey) return;
        try {
            const raw = localStorage.getItem(storageKey);
            if (raw) {
                const parsed = JSON.parse(raw);
                if (Array.isArray(parsed)) {
                    setBookmarks(new Set(parsed.filter((n): n is number => typeof n === "number")));
                }
            }
        } catch (err) {
            console.warn("Failed to load exam bookmarks:", err);
        }
    }, [storageKey]);

    const toggleBookmark = useCallback((questionId: number) => {
        setBookmarks((previous) => {
            const next = new Set(previous);
            if (next.has(questionId)) {
                next.delete(questionId);
            } else {
                next.add(questionId);
            }
            return next;
        });
    }, []);

    const isBookmarked = useCallback(
        (questionId: number) => bookmarks.has(questionId),
        [bookmarks]
    );

    return { bookmarks, toggleBookmark, isBookmarked };
};
