# Test: release test of `develop` before updating `main`

- **Date:** 2026-11-08
- **Branch / commit:** `develop` at `22282d1` (Merge PR #200)
- **Related issues:** #151, #186, #161, #140, #156, #188 (PRs #189, #192, #195, #196, #197, #198, #200)

## What we tested and why

`main` was last updated on 2026-11-05 (PR #191, after the release test of
that day). Since then `develop` has added random practice, the leave
confirmation on the mock exam, Swedish texts across the app, student
feedback, the admin learners list, a `package-lock.json` fix and the mock
exam fixes of #188. This round tests only what changed since 2026-11-05;
practice by subject, statistics, the question bank and accounts are
unchanged and not repeated. #188 was tested in detail before its PR (#200,
"How I tested"); row 6 repeats its main flow on `develop`.

## How

Local backend (IntelliJ) and frontend (`npm run dev`) against a local
PostgreSQL database, with a student account and the admin account. Row 7 on
the deployed app, on a real phone.

### Automated

| Check | Command | Result |
|---|---|---|
| Backend tests | `./mvnw test` (in `backend/`) | ✅ 226 tests, 0 failures |
| Frontend tests | `npx vitest run` (in `frontend/`) | ✅ 40 files, 277 tests passed |
| Frontend build | `npm run build` (in `frontend/`) | ✅ built, no errors |

### Manual

Mark each row ✅ (as expected), ❌ (not as expected; note what happened) or ➖ (not tested).

| # | Step | Expected | Covers | Result |
|---|---|---|---|---|
| 1 | Practice → random practice: answer a few questions, finish | Feedback after each answer; result at the end | #151 | |
| 2 | Click through login, Dashboard, Statistics, Profile, the mock exam result page and the admin start page | Texts in Swedish | #161 | |
| 3 | As a student: "Ge feedback" in the menu, send a message | Confirmation shown | #140 | |
| 4 | Hand in a mock exam, then "Ge feedback om provet" on the result page; then Admin → Feedback | Hand-in works; both messages are listed | #140, #188 | |
| 5 | Admin → Elever: open the list, search for the test student | Student found; details open | #156 | |
| 6 | During a mock exam: click a menu item, then "Stanna kvar"; then "Logga ut", then "Lämna provet" | Asks both times; stays on the exam, then lands on the login page | #186, #188 | |
| 7 | After deployment, on a real phone: log in, start a mock exam, answer, use the overview, hand in | Buttons stay in place; the overview scrolls; result shown | #188, row 18 of 2026-11-05 | |

## Result

(to fill in)

## Conclusion

(to fill in)

## Notes

