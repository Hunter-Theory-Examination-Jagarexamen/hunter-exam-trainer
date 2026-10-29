# Test: Admin question form uses letters for correctAnswer

- **Date:** 2026-10-29
- **Branch / commit:** `feature/155-correct-answer-as-letter-frontend`
- **Related issues:** #155

## What we tested and why

Verified that the admin question form sends the correct answer as a
letter (A/B/C/D) instead of option text, matching the backend change
merged in #172.

## How

### Automated

`frontend/src/__tests__/pages/Admin/QuestionCrud.test.tsx` 22 tests,
including assertions that the POST/PUT body contains `correctAnswer: "C"`
(the letter) rather than the option text.

`frontend/src/__tests__/pages/Admin/QuestionBank.test.tsx` and
`frontend/src/__tests__/components/QuestionImages.test.tsx` updated
test fixtures to use letters.

`npx vitest run` → 172/172 passing.

### Manual

1. Logged in as admin, opened Question Bank.
2. Clicked "Edit question" the correct option radio showed the right
   letter (previously determined by matching text).
3. Changed an option's text without changing the correct option the
   save succeeded with `correctAnswer` still pointing to the same letter.
4. Created a new question, selected option C as correct → POST body had
   `correctAnswer: "C"`.

## Result

All checks passed. Backend and frontend are now consistent: both store
and compare letters.

## Conclusion

The #155 change is complete across backend (#172) and frontend (this PR).

## Notes

- `PracticeQuestions.tsx` was intentionally not touched #170 refactors
  that page and owns it.
