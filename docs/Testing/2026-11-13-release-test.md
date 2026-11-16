# Test: release test of `develop` before updating `main`

- **Date:** 2026-11-13
- **Branch / commit:** `develop` at `9243b94` (Merge PR #226); row 8 and the second frontend run at `923f012` (Merge PR #228)
- **Related issues:** #212, #194, #216, #209, #211, #222, #207 (PRs #213, #215, #217, #218, #219, #220, #226, #227, #228)

## What we tested and why

`main` was last updated on 2026-11-08 (after the release test of that day).
Since then `develop` has added the Terms of Use and Privacy Policy pages,
account deletion (by the user and by an admin), a check that rejects the
login token of a deleted user on every request, the deployed password reset
email, the login button on the 404 page, a shared base for controller tests
and the new app and menu names. PR #228 (practice page buttons stay in
place, #207) was merged during this round; row 8 and a second frontend
run cover it. No table or setting changed. This round
tests only what changed; each PR was tested in detail before it was merged.

## How

Local backend (IntelliJ) and frontend (`npm run dev`) against a local
PostgreSQL database, with the admin account and a throwaway student account
created in row 2. The password reset email is checked on the deployed app after `main` is updated (separate test log).

### Automated

| Check | Command | Result |
|---|---|---|
| Backend tests | `./mvnw test` (in `backend/`) | ❌ on `develop`: `AdminLearnerControllerTests` didn't compile (`LearnerProgressResponse` got a `role` field in #220). Fixed in one line on `fix/learner-progress-test-role`; then ✅ 235 tests, 0 failures |
| Frontend tests | `npx vitest run` (in `frontend/`) | ✅ 44 files, 297 tests passed |
| Frontend build | `npm run build` (in `frontend/`) | ✅ built, no errors |
| Frontend tests and build again, with #228 | `npx vitest run`, `npm run build` | ✅ 44 files, 297 tests passed; built, no errors |

### Manual

Mark each row ✅ (as expected), ❌ (not as expected; note what happened) or ➖ (not tested).

| # | Step | Expected | Covers | Result |
|---|---|---|---|---|
| 1 | Open the login page; open "Användarvillkor" and "Integritetspolicy" from the register page | Title "JägarQuizzen"; both texts open and close | #222, #212 | ✅ Same checks as the #222 test (2026-11-11): title "JägarQuizzen" on login, register and password pages; both texts open from the register page |
| 2 | Register a throwaway student account (tick the terms box), log in | Lands on "Hem" with the menu Hem, Quiz, Övningsprov... | #212, #222 | ✅ Registered "Test Friday"; lands on "Hem" with Hem, Quiz, Övningsprov, Statistik, Profil, Ge feedback; all counts 0, "Ingen aktivitet än." |
| 3 | As that student: a short Quiz round to the result, then Statistik | Result shown; Statistik loads | security check (#194) | ✅ Quiz "Övriga däggdjur": "Du fick 1 av 7 rätt." Statistik: 7 answered questions, one point in "Framsteg över tid" for the week of 9 Nov (about 14 %) |
| 4 | As admin: Deltagare → the throwaway account → "Ta bort deltagarkonto" → confirm, while the student is still logged in in another browser | Account gone from the list | #194 | ✅ Detail page showed Test Friday's activity (Övriga däggdjur 14 %). Dialog "Ta bort Test Fridays konto?" → "Ta bort konto" → back on the list: "Deltagare (14)" without Test Friday ("Friday Test" is an older account) |
| 5 | In the student's browser: click any menu item | Logged out / sent to login; logging in again fails | #194 | ✅ Next click sent the deleted user to `/login?expired=1` with "Din session har gått ut. Vänligen logga in igen." (see Notes). Logging in again not tried |
| 6 | Register a second throwaway account; Profil → "Ta bort konto" with its password | Logged out; logging in again fails | #194 | ✅ "Delete Test": Profil → Riskzon shows the warning and a password field; "Ta bort mitt konto" → login page. Logging in again not tried. Small findings in Notes |
| 7 | Open an unknown URL (e.g. `/xyz`) logged out, then logged in | Logged out: a login button; logged in: "Gå till Hem" | #211, #222 | ✅ `/loginnnnn` logged out: "Gå till inloggningen"; `/dashboardddd` logged in: "Gå till Hem" |
| 8 | Quiz by subject, desktop and a phone-sized window: answer several questions of different length, check answers, go to the next; finish the round | Föregående / Nästa stay in the same place; a new question opens at its start; buttons don't cover the feedback; the result page has no large empty area | #207 | ✅ Phone-sized window (312 × 464): Föregående / Nästa (Slutför on the last question) stay pinned at the bottom for short and long questions and after checking; "Fråga 2 av 7" opens at its start. On this small screen the last line of the feedback or "Rapportera fel i frågan" can sit under the bar (see Notes). Normal phone size (390 × 800): question, "Kontrollera svar" and "Rapportera fel i frågan" all visible above the pinned buttons. Result page ("Du fick 3 av 7 rätt.", and on desktop "2 av 7") has no extra empty area |

## Notes

- Row 5: a deleted user sees "Din session har gått ut" (your session has expired). The user is logged out cleanly, but the message doesn't say the account was removed. Fine for now; a clearer message could be a small later fix.
- Row 8: on a very small screen (312 × 464) the end of the answer feedback, or the "Rapportera fel i frågan" link, can be hidden under the pinned button bar. A clear improvement over before; the remaining part could be a small follow-up for #207.
- Row 6: in the delete-account form, "Avbryt" is an unstyled browser button next to the styled red button, and the "Nuvarande lösenord" label sits tight against its field. Cosmetic only.

