# Test: Report bookmarked mock exam questions from the result page

- **Date:** 2026-11-20
- **Branch / commit:** `feature/199-report-bookmarked-questions`
- **Related issues:** #199

## What we tested and why

After handing in a mock exam, the result page lists the questions the student
bookmarked, each with a "Rapportera fel i frågan" button. The button must open
the feedback dialog with "Fel i en fråga" preset and the right question
attached, so the admin's feedback list shows it like a report from practice.
Nothing may change during the exam, and no list may show when nothing is
bookmarked.

## How

1. Added one component test to `Result.test.tsx`: two bookmarked questions
   (ids 11 and 22), click the report button of the second one, send the
   report, and check that the request body is
   `{ type: "QUESTION", text: "Fel svar", questionId: 22 }`.
2. Ran all frontend tests with `npx vitest run`.
3. Manual check in the running app:
    - Student: bookmarked three questions in a mock exam and handed in.
    - Opened the report dialog from the list and sent a report.
    - Admin: Admin → Feedback shows the report with the question attached.

## Result

- Frontend tests: **307/307 pass** (306 existing + 1 new).
- The report from the list reached Admin → Feedback as "Fel i en fråga",
  with the question shown.

## Conclusion

A report from the bookmarked list contains the right question. No backend
or API change was needed. Bookmarks are reused as they are, and the list
disappears on reload, like the result does today.

## Manual verification

- [x] Report from the list shows up in Admin → Feedback with the question
- [ ] A mock exam without bookmarks shows no list on the result page
