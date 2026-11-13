# Test: release test of `develop` before updating `main`

- **Date:** 2026-11-13
- **Branch / commit:** `develop` at `9243b94` (Merge PR #226)
- **Related issues:** #212, #194, #216, #209, #211, #222 (PRs #213, #215, #217, #218, #219, #220, #226)

## What we tested and why

`main` was last updated on 2026-11-08 (after the release test of that day).
Since then `develop` has added the Terms of Use and Privacy Policy pages,
account deletion (by the user and by an admin), a check that rejects the
login token of a deleted user on every request, the deployed password reset
email, the login button on the 404 page, a shared base for controller tests
and the new app and menu names. No table or setting changed. This round
tests only what changed; each PR was tested in detail before it was merged.

## How

Local backend (IntelliJ) and frontend (`npm run dev`) against a local
PostgreSQL database, with the admin account and a throwaway student account
created in row 2. Row 8 on the deployed app after `main` is updated.

### Automated

| Check | Command | Result |
|---|---|---|
| Backend tests | `./mvnw test` (in `backend/`) | ❌ on `develop`: `AdminLearnerControllerTests` didn't compile (`LearnerProgressResponse` got a `role` field in #220). Fixed in one line on `fix/learner-progress-test-role`; then ✅ 235 tests, 0 failures |
| Frontend tests | `npx vitest run` (in `frontend/`) | ✅ 44 files, 297 tests passed |
| Frontend build | `npm run build` (in `frontend/`) | ✅ built, no errors |

### Manual

Mark each row ✅ (as expected), ❌ (not as expected; note what happened) or ➖ (not tested).

| # | Step | Expected | Covers | Result |
|---|---|---|---|---|
| 1 | Open the login page; open "Användarvillkor" and "Integritetspolicy" from the register page | Title "JägarQuizzen"; both texts open and close | #222, #212 | |
| 2 | Register a throwaway student account (tick the terms box), log in | Lands on "Hem" with the menu Hem, Quiz, Övningsprov... | #212, #222 | |
| 3 | As that student: a short Quiz round to the result, then Statistik | Result shown; Statistik loads | security check (#194) | |
| 4 | As admin: Deltagare → the throwaway account → "Ta bort deltagarkonto" → confirm, while the student is still logged in in another browser | Account gone from the list | #194 | |
| 5 | In the student's browser: click any menu item | Logged out / sent to login; logging in again fails | #194 | |
| 6 | Register a second throwaway account; Profil → "Ta bort konto" with its password | Logged out; logging in again fails | #194 | |
| 7 | Open an unknown URL (e.g. `/xyz`) logged out, then logged in | Logged out: a login button; logged in: "Gå till Hem" | #211, #222 | |
| 8 | Deployed app, after `main` is updated: "Glömt lösenord" with your own email | Mail arrives, sender "JägarQuizzen" (after `MAIL_FROM` is changed on Render) | #209, #222 | |

## Notes

