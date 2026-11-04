# Test: Exam overview, bookmarks, submit-from-any, i18n

- **Date:** 2026-11-04
- **Branch / commit:** `feature/175-exam-overview-and-bookmarks`
- **Related issues:** #175

## What we tested and why

Verified the three exam UX improvements:

1. **Submit from any question** → the Submit button is always visible; students can hand in from any question (not only the last).
2. **Overview grid + bookmarks** → the student sees all 70 questions, can jump to any, bookmark for later, and see answered / unanswered / bookmarked / current at a glance.
3. **i18n** → the exam page renders in Swedish by default; all strings are in `sv.json` / `en.json`.

## How

### Automated

`frontend/src/__tests__/pages/Exam/Exam.test.tsx` — 13 tests:
- Loading, error states
- Saves each answer to server on selection (does not save twice)
- Submit sends only `attemptId`
- Disables Next on last question
- Submit-from-Q1 shows confirmation modal with "Submit anyway" button
- Auto-submit fires on time-out

`frontend/src/__tests__/components/exam/ExamOverview.test.tsx` — 5 tests:
- Renders all numbers
- Marks current with `aria-current`
- Marks answered / unanswered correctly
- Marks bookmarked correctly
- Click → `onJumpToQuestion(index)`

`frontend/src/__tests__/hooks/useExamBookmarks.test.ts` — 5 tests:
- Empty initially
- Toggle on/off
- Persists to localStorage
- Per-attempt isolation
- No-op while `attemptId` is null

`frontend/src/__tests__/components/QuestionImages.test.tsx` updated to pass `isBookmarked` / `onToggleBookmark` to `QuestionPanel` (regression for `tsc -b`).

Full suite: `npx vitest run` → **205/205 tests pass, 28 files**. `npx tsc -b` clean. `npm run build` succeeds.

### Manual

1. **Desktop:**
    - Opened the exam page.
    - Overview grid visible on the right; answered questions filled + ✓.
    - Clicked a question in the grid → jumped to it.
    - Bookmarked a question → 🔖 appears in the grid; button label switches to "Bokmärkt" (Swedish).
    - Clicked "Lämna in provet" from Q1 (not the last) → confirmation modal appeared: "Du har 2 obesvarade frågor. De räknas som fel." Confirmed → result page.
    - Toggled the "Översikt" button in the header no visible change on desktop (as expected; it's a mobile toggle).

2. **Mobile (DevTools device mode at 360×800):**
    - Overview hidden by default; "Översikt" button visible in the header.
    - Tapping "Översikt" slides in a full-screen overlay grid.
    - Tapping a question closes the overlay and jumps.
    - Submit button is full-width below Previous/Next.
    - Confirmation modal fits on screen.

3. **Language:**
    - Default locale `sv` → all exam labels Swedish.
    - Manually switched to `en` via DevTools (`i18n.changeLanguage("en")`) → all exam labels English.

## Result

All checks passed. All three features work as specified.


## Conclusion

#175 is complete. The exam page now supports overview + bookmarks + submit-from-any in both languages. The submit confirmation copy is reusable by the upcoming leave-exam prompt issue.

## Notes

- Overview + bookmarks merged in PR #181; i18n merged in this PR.
- Bookmarks are localStorage-only (per attempt); server-side is a follow-up if we want cross-device.
- Two UX improvements noted during review (sticky nav bar, Recent Results ✓ icon) — filed as separate issues.
- Timer clamps at 0 to prevent the `-1:-1` display edge case (pre-existing bug surfaced under test load).
- 
