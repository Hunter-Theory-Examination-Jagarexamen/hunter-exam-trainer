# Test: password reset email (local and deployed)

- **Date:** 2026-11-09 (local) and 2026-11-10 (deployed)
- **Branch:** `fix/209-deployed-password-reset-email`; deployed app runs `main`
- **Related issues:** #209
- **Tester:** Hans

## What we tested and why

Before #209, "Glömt lösenord?" sent no email on the deployed app: the backend
on Render had no mail settings, and Render's free plan blocks the usual mail
ports (25, 465, 587). #209 sets up Brevo (SMTP relay, port 2525) and makes the
email and page texts Swedish.

1. **Local, through Brevo:** the whole flow with the branch's code, including
   the Swedish texts.
2. **Deployed:** the whole flow on `https://hunter-exam.vercel.app` with the
   new Render settings. The deployed app runs `main`, which doesn't have the
   Swedish texts yet, so English texts are expected there.

## How

### Automated

- Backend: `./mvnw test` (226 tests, 2026-11-09, before merging `develop`
  with #215), including `PasswordResetEmailServiceTests` (Swedish phrases and
  the reset link) and `PasswordResetControllerTests`.
- Frontend: `npx vitest run` (43 files, 289 tests, 2026-11-10), including
  `ForgotPassword.test.tsx` and `PasswordRecovery.test.tsx` (the Swedish
  success texts).

### Manual

Local: backend in IntelliJ with the Brevo values in the run configuration
(see README, "Password Reset Email (Brevo)") and
`FRONTEND_URL=http://localhost:5173`. Deployed: Render settings filled in by
Sikdar, account registered on the deployed app with an inbox the tester can
read.

| # | Step | Expected | Result |
|---|---|---|---|
| 1 | Local: "Glömt lösenord?", enter the account's email | Confirmation on the page; email arrives | ✅ Email in the Gmail inbox (not spam) within a minute (2026-11-09 18:25). Page text was the backend's English message (before the Swedish change) |
| 2 | Local: check the sender | Name "Jägarexamen Tränare" | ✅ "Jägarexamen Tränare", "ä" correct. Address rewritten by Brevo to `hunterexam.trainer@12729578.brevosend.com` (expected for a Gmail sender) |
| 3 | Local: follow the link, set a new password | Reset page opens; success message | ✅ Link opened `http://localhost:5173/reset-password`, password changed |
| 4 | Local: log in with the new password | Logged in | ✅ Dashboard shown |
| 5 | Local, after the Swedish change: repeat 1 and 3 | Swedish email and page texts | ✅ (2026-11-10 16:43) Page: "Om det finns ett konto för den e-postadressen har vi skickat en länk … Kolla även skräpposten." Email subject "Återställ ditt lösenord", Swedish text, in the inbox. After the reset: "Lösenordet är ändrat. Nu kan du logga in." |
| 6 | Deployed: "Glömt lösenord?" on `hunter-exam.vercel.app` | Confirmation; email arrives | ✅ Email in the Gmail inbox at 16:35 (2026-11-10), English text (expected, `main`) |
| 7 | Deployed: check the link | Points to the deployed frontend | ✅ `https://hunter-exam.vercel.app/reset-password#token=...` |
| 8 | Deployed: set a new password | Success message | ✅ "Password reset successfully. You can now log in." (English, expected) |
| 9 | Deployed: log in with the new password | Logged in | ✅ Dashboard shown for the test account |

## Result

Password reset emails are sent and arrive in the inbox, locally and on the
deployed app, and the whole flow works end to end. The Swedish email and page
texts reach the deployed app with the next release to `main`.

## Follow-ups

- Next release test: one row for "Glömt lösenord?" to check the Swedish email
  and page texts on the deployed app.
- The SMTP key expires in May 2027 (README).
