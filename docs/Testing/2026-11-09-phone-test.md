# Test: the deployed app on a real phone

- **Date:** 2026-11-09
- **Deployment:** frontend `https://hunter-exam.vercel.app` (Vercel), backend
  on Render, both from `main` after the 2026-11-08 release
- **Related:** row 7 of `2026-11-08-release-test.md`; #188 (exam page on
  phones); row 18 of `2026-11-05-release-test.md` (overview scrolling)

## What we tested and why

The release test of 2026-11-08 ran locally. This test checks the deployed
app on a real phone before the user test the same day: that the frontend
runs our `main` (Vercel built from another repository until 2026-11-09), and
that the mock exam works by touch on a small screen.

## How

Real phone: Android phone, Chrome (screenshot via the Claude app). Logged in with a student account on the
deployed app.

Mark each row ✅ (as expected), ❌ (not as expected; note what happened) or ➖ (not tested).

| # | Step | Expected | Result |
|---|---|---|---|
| 1 | Open `hunter-exam.vercel.app` | Login page in Swedish (our `main`), no "Continue with Guest" | ✅ "Välkommen tillbaka!", no Google or Guest button: the frontend now runs our `main`. On a cold start the banner went orange, then red, then away after trying again (see Notes) |
| 2 | Log in as a student | Instrumentpanel opens; menu (☰) opens and closes | ✅ Instrumentpanel opens (0 exams, a new account). Header shows ☰, logo and title; "Välkommen, …" is hidden at phone width (space saving, also on desktop at 312 px) |
| 3 | Start a mock exam; answer a few questions with Nästa / Föregående, on a short and a long question | Buttons stay at the bottom in the same place; a new question opens at its start | ✅ Föregående / Nästa stay at the bottom; "Lämna in provet" below the answers. The first few questions did not scroll back to their start, later ones did (see Notes) |
| 4 | Open the overview, scroll it by touch, jump to a question | The overview scrolls; the jump works | ✅ Opens over the exam; all 70 numbers fit on this phone, so the overview itself didn't need to scroll. A thumb swipe on it scrolled the exam page behind it (see Notes). Tapping 68 opened "Fråga 68 av 70" at the top of the page |
| 5 | Hand in ("Lämna in provet" below the answers) and confirm | Pop-up over the exam; result page shown | ✅ From the last question ("Lämna in provet" in Nästa's place): pop-up over the exam, "Du har 66 obesvarade frågor…", "Lämna in ändå". Result page: 1 %, 1 / 70 Rätt (1 right, 3 wrong, 66 unanswered) |

## Result

All 5 rows as expected on a real phone (Android, Chrome). The deployed
frontend now runs our `main` (Swedish login page). Findings, none blocking
the user test:

- Cold start: the server banner turns red while the server is only waking
  (`docs/local/known-gaps/server-banner-red-while-waking.md`); clicking again
  works.
- The first visit after a deployment may run the old cached app once (PWA),
  e.g. questions not opening at their start.
- Swiping on the open overview scrolls the exam page behind it.

## Conclusion

The deployed app is ready for the user test on 2026-11-09.

## Notes
- Row 1: on a cold start the banner showed orange "Servern startar…", then
  red "Det går inte att nå servern…" while the server was still waking; it
  went away after logging in again. Not a blocker (click again); logged in
  `docs/local/known-gaps/server-banner-red-while-waking.md`.
- Row 3: for the first few questions, a new question kept the scroll
  position (Hans had to scroll up); after that it worked. Likely cause: the
  app is a PWA (`registerType: 'autoUpdate'`), so the phone first ran the
  version cached from the earlier deployment, and the new one took over in
  the background. Rechecked with a new exam (07:47): every new question
  opened at its start from the first one, which fits the cache explanation.
  Testers who visited before the release may see this once.
- Row 4: swiping on the open overview scrolled the exam page behind it (the
  browser's address bar hid and the panel moved). The overview fits on this
  phone, so the swipe passes through to the page ("scroll chaining"). Small:
  `overscroll-behavior: contain` on the panel, or no page scrolling while it
  is open. The "Översikt" button is small but findable (Hans).
