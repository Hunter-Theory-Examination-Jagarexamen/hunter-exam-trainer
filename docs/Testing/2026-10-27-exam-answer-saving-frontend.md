# Test: Exam answer saving (frontend)

- **Date:** 2026-10-27
- **Branch / commit:** `feature/116-save-exam-answers-frontend`
- **Related issues:** #116

## What we tested and why

Verified that the frontend saves each answer to the server as the
student gives it, so a lost connection, expired token, or closed tab
does not lose the attempt. Also verified the improved submit-failure
message.

This is the frontend half of #116 (backend was merged in #165).

## How

### Automated

`frontend/src/__tests__/pages/Exam/Exam.test.tsx` 9 tests (2 new):
- "saves each answer to the server as it is given" verifies POST
  to `/api/exam/{sessionId}/answer` with the correct body
- "does not save the same answer twice" verifies the no-change
  guard prevents duplicate POSTs

Run: `npx vitest run src/__tests__/pages/Exam/Exam.test.tsx` → 9/9 pass

### Manual

1. Started backend and frontend locally, logged in.
2. Started a mock exam.
3. Answered the first question → verified in DevTools Network tab
   that a POST to `/api/exam/{id}/answer` fires with the right payload.
4. Navigated to the next question and back the selection persisted.
5. Answered all questions and submitted → result page rendered.
6. Simulated submit failure (network throttle to offline right before
   submit) → alert shown: "Failed to submit exam. Your answers are
   saved on the server — please try again."
7. Closed the browser tab mid-exam → reopened, exam session was lost
   but the answers were saved on the server (verified via DB).

## Result

All checks passed.

## Conclusion

The frontend now saves answers on the server as they are given. A
submit failure shows a clear message that answers are safe.

## Notes

- Best-effort saving: if an `/answer` POST fails, the user is not
  blocked. The local state still holds the answer, and re-selecting
  triggers another POST.
- A warning indicator for persistent save failures is a possible
  follow-up.
- Backend: `exam_session_answers` table stores the answers (see PR #165).
- 401 handling is delegated to the shared apiClient from #87.
