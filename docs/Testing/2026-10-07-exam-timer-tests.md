# Test Log: Exam Timer & Navigation Tests (#90)

**Date:** 2026-10-07
**Scope:** `src/__tests__/pages/Exam/Exam.test.tsx`

## Overview

Added unit/integration tests for the mock exam page timer and navigation features as requested in issue #90:
- Verification of timer display and formatting (`60:00` -> `59:59`).
- Verification of auto-submission when time expires using Vitest fake timers (`advanceTimersByTimeAsync`) ensuring partially answered questions are sent.
- Verification of `ExamNavigation` behaviour (`Previous` button disabled on first question, enabled on subsequent questions).

## Test Results

```text
 ✓ src/__tests__/pages/Exam/Exam.test.tsx (6 tests)
   ✓ Exam page (6)
     ✓ shows a loading message, then the first question
     ✓ submits the session id, question ids and answers, then shows the result page
     ✓ shows an error message when the exam cannot be loaded
     ✓ disables Previous on the first question and enables it on subsequent questions
     ✓ displays the timer countdown in the header
     ✓ automatically submits the exam with current answers when time runs out

 Test Files  7 passed (7)
      Tests  35 passed (35)
   Start at  16:13:43
   Duration  26.84s (environment 46%, tests 21%, setup 14%, transform 12%, import 7%)