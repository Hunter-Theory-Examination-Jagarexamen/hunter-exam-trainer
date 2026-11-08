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
| 1 | Practice → random practice: answer a few questions, finish | Feedback after each answer; result at the end | #151 | ✅ "Slumpmässig övning", 20 questions; feedback after each answer ("Rätt!"). Quit after 10 with "Avsluta omgången": "Du svarade på 10 av 20 frågor och fick 3 rätt." |
| 2 | Click through login, Dashboard, Statistics, Profile, the mock exam result page and the admin start page | Texts in Swedish | #161 | ✅ As admin: login, Instrumentpanel, Statistik, Profil, the result page ("Resultat", 7 %, 5 / 70 Rätt) and the admin start page are all in Swedish (row 17 of 2026-11-05 now done). Small findings in Notes |
| 3 | As a student: "Ge feedback" in the menu, send a message | Confirmation shown | #140 | ✅ As student "Release Test": "Ge feedback", type "Appen", message sent: "Tack! Ditt meddelande har skickats till kursledarna." Admin → Feedback lists it (Appen, 2026-11-08 21:35:26) with "Markera som hanterad" |
| 4 | Hand in a mock exam, then "Ge feedback om provet" on the result page; then Admin → Feedback | Hand-in works; both messages are listed | #140, #188 | ✅ Hand-in worked (result page: 7 %, 5 / 70 Rätt). "Ge feedback om provet" sent; Admin → Feedback lists it (2026-11-08 21:41:20) with the message from row 3. Filed as type "Appen" (see Notes) |
| 5 | Admin → Elever: open the list, search for the test student | Student found; details open | #156 | ✅ Elever lists "Administratörer (1)" and "Studenter (14)" by name; "Release Test" opens its details: last activity, 4 mock exams, latest 0 % · "Ej godkänd", best 4 %, and per-subject percentages. There is no search field (the list has no search; the step's "search" was wrong), finding a name means scrolling |
| 6 | During a mock exam: click a menu item, then "Stanna kvar"; then "Logga ut", then "Lämna provet" | Asks both times; stays on the exam, then lands on the login page | #186, #188 | ✅ Menu click: "Lämna provet?" shown; "Stanna kvar" kept the exam (question 5 answered after it: 4 → 5 / 70 besvarade). "Logga ut": asked again; "Lämna provet" led to the normal login page (no "session expired", no browser prompt) |
| 7 | After deployment, on a real phone: log in, start a mock exam, answer, use the overview, hand in | Buttons stay in place; the overview scrolls; result shown | #188, row 18 of 2026-11-05 | ➖ Not part of this round: needs the deployed app, which isn't ready (see Notes). Tested separately in a phone test log |

## Result

- Automated: backend 226 tests, frontend 277 tests and the frontend build
  all pass.
- Manual: rows 1–6 as expected. Row 7 is left to a separate phone test
  on the deployed app (see Conclusion).
- No errors that block use. Smaller findings are in the Notes and in
  `docs/local/known-gaps/` (wording, exam feedback type) and
  `design-notes/` (search in the learners list).

## Conclusion

`develop` at `22282d1` can be merged into `main`.

Before the user test on 2026-11-09:
- The Vercel frontend must build from our repository's `main` (row 7,
  asked Sikdar 2026-11-08). Check: the login page is in Swedish.
- Then a separate phone test on the deployed app (row 7), in its own log:
  `docs/Testing/2026-11-09-phone-test.md`.

## Notes
- Row 2: Profile shows the role as "ADMIN" (the value from the database,
  not translated). The admin start page's card says "Studenter" while the
  menu says "Elever".
- Row 2, still open from 2026-11-05: the result page shows no pass/fail
  (row 8 then), and Statistik → Senaste resultat puts a check mark on every
  mock exam, also 0 % ones.
- Row 4: "Ge feedback om provet" on the result page opens the same dialog as
  the menu, with "Vad gäller det?" set to "Appen" (choices: Fel i en fråga,
  Appen, Övrigt). Exam feedback is filed as "Appen" unless the student
  changes it, and admins can't tell it came from the result page.
- Row 7: `hunter-exam.vercel.app` shows an English login page with
  "Continue with Guest", which is not in our code. Sikdar's Vercel screenshot
  (2026-11-05) shows it deploys `main` at commit `8c1b099` "Initial commit",
  which is not in our repository, so Vercel is connected to another
  repository. Render (backend) uses our repository, `main`, `backend/`.
