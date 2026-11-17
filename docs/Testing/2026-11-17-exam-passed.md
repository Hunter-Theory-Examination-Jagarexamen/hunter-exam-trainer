# Test: Mock exam result page passed / not passed

- **Date:** 2026-11-17
- **Branch / commit:** `feature/exam-result-passed`
- **Related issues:** #205

## What we tested and why

The mock exam result page showed the score and the answer breakdown but not
whether the exam was passed. The backend had the rule (60 of 70 correct)
but only used it on the admin's learner detail view. #205 asks for three
things:

1. The result page must clearly say "Godkänt" / "Ej godkänt"
   (and "Passed" / "Not passed" in English).
2. The pass mark (60 of 70) must live in **one place** in the backend, shared
   by the result page and the admin's view — the frontend must not repeat the
   number.
3. A backend test must cover the boundary (60 passes, 59 fails).

## How

**Automated, backend**

```bash
cd backend
./mvnw test -Dtest="ExamScoringTest,ExamServiceTests"
