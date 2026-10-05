# Test: First frontend unit and component tests

- **Date:** 2026-10-05
- **Branch / commit:** `feature/38-frontend-tests`
- **Related issues:** #38 (also covers the contract from #35 / #49)

## What we tested and why

The frontend had no automated tests, only lint and the TypeScript build. Issue #38 asked for tests of key components and flows. We added a test runner and tests for the parts where a bug would hurt users most: logging in, registering, practising and taking the mock exam.

| Test file (under `frontend/src/__tests__/`) | What it covers | Tests |
|---|---|---|
| `components/statistics/ProgressBar.test.tsx` | subject name, percentage text, fill width, click | 4 |
| `components/auth/LoginForm.test.tsx` | empty fields, successful login (token saved, goes to dashboard), failed login | 3 |
| `components/auth/RegisterForm.test.tsx` | the four validation rules, successful sign-up, backend error shown | 6 |
| `components/practice/PracticeQuestion.test.tsx` | answer choices, selecting an answer, Check Answer disabled, Correct/Incorrect feedback | 5 |
| `pages/Exam/Exam.test.tsx` | loading, submit sends `sessionId`, question ids and answers (the #35/#49 contract), load error | 3 |

The backend, router and browser pop-ups are replaced with mocks, so the tests run without a server or database.

## How

1. **Tools:** Vitest (test runner) with jsdom (simulated browser), React Testing Library and user-event (find and use elements the way a user does) and jest-dom (extra checks such as `toBeInTheDocument`). Configured in `frontend/vite.config.ts`; shared setup in `frontend/src/__tests__/setupTests.ts`.
2. **Layout:** tests live in `frontend/src/__tests__/`, mirroring the folders under `src/` (like Maven's `src/test`). Test files end in `.test.tsx`; they are not part of the production build.
3. **Run:** in `frontend/`, `npm install`, then `npx vitest run` (or `npm test` to keep watching for changes).
4. **Checking that the tests can fail:** each component was temporarily broken in a way matching one test (for example, the exam no longer sending `sessionId`, registration accepting 4-character passwords), the tests were run, and the changes were undone.

## Result

- Normal run: **5 test files, 21 tests passed**. No warnings in the output.
- With the components deliberately broken: **21 of 21 tests failed**, so every test detects the problem it is meant to catch.
- `npm run build` still passes with the test setup in place.

## Conclusion

The frontend now has a test suite for its key components and flows, and it would have caught #49 before merge. Follow-up ideas, not part of #38:

- More tests: exam timer auto-submit, login protection of pages, password reset, Google login redirect, `authService` / `apiClient`.
- End-to-end tests (for example Playwright) for a few full flows with the real backend.
- Running the tests automatically on every pull request (GitHub Actions).
