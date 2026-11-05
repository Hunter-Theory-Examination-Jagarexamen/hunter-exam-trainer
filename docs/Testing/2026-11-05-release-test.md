# Test: release test of `develop` before updating `main`

- **Date:** 2026-11-05
- **Branch / commit:** `develop` at `3d5d2a2` (Merge PR #185)
- **Related issues:** #168, #170, #174, #175, #180, #183 (store-each-answer refactor and mock exam navigation), #155, #161

## What we tested and why

`main` was last updated on 2026-10-15. Since then `develop` has changed how
results are stored (every answer per question), the mock exam, the statistics
pages, the correct answer as a letter, and the Swedish texts. This round checks
that the whole app still works together before `develop` is merged into `main`
and deployed for the user testing on 2026-11-09.

## How

Local backend (IntelliJ) and frontend (`npm run dev`) against a local
PostgreSQL database, with an existing student account that already has
results, a new student account, and the admin account.

### Automated

| Check | Command | Result |
|---|---|---|
| Backend tests | `./mvnw test` (in `backend/`) | ✅ 169 tests, 0 failures |
| Frontend tests | `npx vitest run` (in `frontend/`) | ✅ 28 files, 205 tests passed |
| Frontend build | `npm run build` (in `frontend/`) | ✅ built, no errors |

### Manual

Mark each row ✅ (as expected), ❌ (not as expected; note what happened) or ➖ (not tested).

**Account**

| # | Step | Expected | Result |
|---|---|---|---|
| 1 | Register a new student account | Confirmation shown; can log in | ✅ "Account created successfully! You can now log in." |
| 2 | Log out and log in again | Lands on the Dashboard | ✅ empty Dashboard (0 answered), "Welcome, Release Test" |
| 3 | "Forgot password" with the new account's email | Message that an email is sent (local: link in the backend log or mail) | ✅ With Mailpit: email arrived, link opened the reset page, new password set, login with it works. The new password may be the same as the old one, and the email isn't carried over from the login page (see Notes) |

**Practice**

| # | Step | Expected | Result |
|---|---|---|---|
| 4 | Practice by subject: answer all questions, finish | Feedback after each answer; result screen at the end | ✅ Jaktetik: "Du fick 3 av 7 rätt." |
| 5 | Start a round, answer 2–3 questions, quit | Asks first; shows "answered X of Y" | ✅ Introduktion: "Du svarade på 2 av 7 frågor och fick 2 rätt." |
| 6 | Start a round, click a menu item | Asks before leaving | ✅ "Avsluta omgången?" with Avsluta / Fortsätt öva. With no answers yet it still says "Dina svar är redan sparade." (see Notes) |

**Mock exam**

| # | Step | Expected | Result |
|---|---|---|---|
| 7 | Start a mock exam, answer some questions, use the overview and a bookmark | Overview shows answered and bookmarked questions; jumping works | ✅ "Översikt · 8 / 70 besvarade", questions 1–8 marked answered, question 9 bookmarked (marker in the overview), jump from the overview to question 19 works |
| 8 | Change an answer, then hand in with unanswered questions left | Confirmation says unanswered count as wrong; result with score and pass/fail | ⚠️ Changing an answer works. Confirmation: "Du har 62 obesvarade frågor. De räknas som fel." (shown at the bottom of the page, not as a pop-up: #188). Result: 4 %, 3 / 70, 3 correct, 5 incorrect, 62 unanswered. The result page shows no pass/fail, and its texts are in English (see Notes) |
| 9 | Start a mock exam and click a menu item | Known: leaves at once without asking (#186) | ✅ as known: clicking Profile during the exam opened Profile at once, no question asked (#186). The exam page also shows "Answered 0 / 70" in English above the Swedish overview (see Notes) |

**Statistics**

| # | Step | Expected | Result |
|---|---|---|---|
| 10 | Dashboard after 4–8 | Questions answered and mock exams went up; recent activity shows the exam | ✅ Mock Exams 1, Questions Answered 17 (practice 7 + 2, mock exam 8 answered), average and best 4 %; Recent Activity shows the mock exam (4 %, 3/70). Practice rounds are not listed there (known, see Notes) |
| 11 | Statistics | Same per-subject numbers as Learner Progress; best and average score match the mock exams | ✅ Same cards as the Dashboard. Per subject: Introduktion 100 %, Jaktetik 43 % (3 of 7), and the mock exam's 8 answered questions in their subjects (3 right, 5 wrong). Progress over Time: 47 % for the week of 2 Nov (8 of 17 right) |
| 12 | Admin → Learner Progress for the student | Same per-subject numbers as the student's Statistics; latest mock exam with pass/fail | ✅ All 9 subjects match Statistics; "Mock exams: 1 taken, Latest: 4 % (2026-11-05) · Not passed, Best: 4 %" |

**Admin**

| # | Step | Expected | Result |
|---|---|---|---|
| 13 | Admin start page | Counts for questions, subjects, students | ✅ Questions 100, Subjects 15, Learners 14 |
| 14 | Question Bank: edit a question's correct option, save | Saved; practising that question uses the new correct option | ✅ Question #1: option B's text changed to "71 frågor", correct answer kept as A. "Question #1 updated"; the bank shows "Correct answer: A"; in practice the new text shows and A ("50 frågor") is marked right |
| 15 | Create and delete a test question | Works; the question bank count changes | ✅ Created #101 "Test question" in Introduktion; it shows in the bank with correct answer A. Deleted it again: the admin start page shows 100 questions |
| 16 | As a student, open `/admin` | No access | ✅ As student, `/admin/` sends the student to the Dashboard |

**General**

| # | Step | Expected | Result |
|---|---|---|---|
| 17 | Texts on login, practice and mock exam pages | Swedish | ➖ Not expected yet: #161 (all texts in Swedish) is only partly merged. The practice and mock exam pages are Swedish; login, Dashboard, Statistics, Profile, the mock exam result page and the admin pages are still English |
| 18 | Phone-sized window (browser dev tools): login, practice, mock exam | Usable; menu opens and closes | ⚠️ Firefox responsive mode, 320 × 480. The menu (☰) opens and closes; Dashboard, practice (answer + feedback) and the mock exam work, and the overview opens as its own panel. The mock exam overview could not be scrolled with the mouse wheel (see Notes). "Fråga 1 av 70" wraps onto three lines next to Översikt and the timer |
| 19 | Stop the backend while logged in, then use the app | Clear "can't reach the server" message | ✅ Red banner "Can't reach the server. Check your internet connection, or try again in a few minutes." with "Try again" (English, #161). The console shows the failed requests as CORS errors, which is how the browser reports a server that doesn't answer |

## Result

- Automated: backend 169 tests, frontend 205 tests and the frontend build
  all pass.
- Manual: 16 of 19 rows as expected (1–7, 9–16, 19; row 9 is the known
  #186 behaviour). Two rows with remarks: 8 (the result page shows no
  pass/fail and is in English) and 18 (the overview's mouse-wheel scrolling
  in the phone simulation, to check on a real phone). Row 17 not expected
  yet (#161 partly merged).
- The statistics agree everywhere: Dashboard, Statistics, Progress over Time
  and Learner Progress show the same numbers, and they add up from the
  practice rounds and the mock exam taken in this test.
- No errors that block use were found. Smaller findings are in the Notes.

## Conclusion

`develop` at `3d5d2a2` can be merged into `main`. Later PRs (e.g. #151
Random Practice) are not part of this test and not part of this release.

Before the user test on 2026-11-09:
- Mail settings on the deployment, or students can't reset passwords (row 3).
- Check the mock exam overview on a real phone (row 18).
- Consider showing pass/fail on the result page (row 8).
- The app is partly English until #161 is finished (row 17).

## Notes

- Row 3 needs a mail server. Locally it was tested with Mailpit (README,
  "Password reset"). Without one, the page shows the same message and nothing
  is sent (on purpose, so it doesn't reveal which emails have accounts).
- Small findings, not errors: a reset accepts the current password as the new
  one; passwords only need 8 characters; "Forgot password?" doesn't fill in
  the email typed on the login page.
- Row 6: the leave prompt says "Dina svar är redan sparade." also before any
  question is answered. A wording change would fix it.
- Row 8: the mock exam result page (`Result.tsx`) shows the score but not
  whether the exam is passed (Learner Progress shows "Not passed"), and its
  texts are still in English.
- Row 9: the exam page shows "Answered 0 / 70" in English above the question,
  next to the Swedish "Översikt · 0 / 70 besvarade" (the same number twice).
- Rows 10–11: Recent Activity (Dashboard) and Recent Results (Statistics) list
  only mock exams, not practice rounds (known). In Recent Results the failed
  4 % exam has a check mark, which can read as "passed".
- Row 13: the admin start page works, but its three large cards feel more
  like a placeholder than an admin overview (Hans; improve later).
- Row 14: the question bank list is not sorted by question number.
- Row 16: a student opening `/admin` is sent to the Dashboard without a
  message. The backend refuses admin requests from students (403) either way.
- Row 18: in the phone-sized window the mock exam overview didn't scroll with
  the mouse wheel. Firefox's responsive mode had touch simulation on, which
  can turn the wheel off. A drag couldn't be done in the simulation. The
  phone layout's overview panel is set to scroll (`overflow-y: auto` in
  `exam.css`), so it should work on a real phone; check on one before the
  user test. Switching to the phone view also logged the admin
  out with "Your session has expired" (the expired-login message works).
