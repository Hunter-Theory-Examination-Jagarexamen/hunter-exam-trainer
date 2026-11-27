# Test: Server banner red state and background check (#254)

- **Date:** 2026-11-25
- **Branch / commit:** fix/254-server-banner-red-state
- **Related issues:** #254 (follow-up to #206)

## What we tested and why

When the backend is really down, the banner turns red after 90 s and shows "Försök igen". Before this change every failed request set the status back to "starting", so the banner turned orange again and the button disappeared. We tested that a failed request while the banner is red keeps it red.

## How

1. In `ServerStatusBanner.test.tsx`, add the test "a failed request while the banner is red keeps it red (#254)".
2. Run `npx vitest run ServerStatusBanner` before the fix. The new test must fail.
3. Add `markServerStarting()` in `serverStatus.ts` and use it in the three places in `apiClient.ts` that used to set "starting".
4. Run `npx vitest run ServerStatusBanner` again, then `npx vitest run`.

## Result

- Before the fix: 6 passed, 1 failed. The banner was orange (`role="status"`) instead of red (`role="alert"`).
- After the fix: 7 of 7 passed in `ServerStatusBanner`.
- Full run: 46 files, 326 tests passed.
- Not yet tested in the running app. That is done when the later steps of #254 are finished.

## Conclusion

The test reproduces the bug and the fix removes it. The remaining parts of #254 (one background check at a time, a check that sees Render's error page) are separate steps and will add to this log or get their own.

## Step 3: only one background check at a time

**What and why:** While the banner is orange or red it checks the server every 3 s. A new check started even if the previous one had not answered, so several could be open at once while Render wakes up. We tested that a hanging check blocks new ones.

**How:**
1. Add the test "runs at most one background check at a time (#254)": `fetch` never answers, 10 s pass with fake timers, `fetch` must be called once.
2. Run `npx vitest run ServerStatusBanner` before the fix. The new test must fail.
3. Add a `checkInFlight` flag (`useRef`) in `ServerStatusBanner.tsx`; `check` returns early while it is set and clears it in `finally`.
4. Run `npx vitest run ServerStatusBanner` again, then `npx vitest run`.

**Result:**
- Before the fix: 7 passed, 1 failed (`fetch` called 3 times instead of 1).
- After the fix: 8 of 8 passed in `ServerStatusBanner`.
- Full run: 46 files, 327 tests passed.
- Not yet tested in the running app.

**Conclusion:** The test reproduces the bug and the fix removes it. "Try again" is unchanged and has its own check. A check that sees Render's error page is still to do.

## Step 5: the background check no longer trusts a host error page

**What and why:** The background check used `mode: "no-cors"`, which only fails when nothing answers at all. Render's error page (502/503) counted as an answer, so the banner was hidden, the next request failed and showed it again, and a server that was down might never turn red. The check is now a normal CORS request and does not count 502/503/504 as an answer. We tested that a 503 does not hide the banner.

**How:**
1. Add the test "a 503 from the host does not hide the banner (#254)": `fetch` answers 503, 3 s pass with fake timers, `fetch` must be called once and the "starting" banner must still be shown.
2. Run `npx vitest run ServerStatusBanner` before the fix. The new test must fail.
3. In `ServerStatusBanner.tsx`, remove `mode: "no-cors"` from the background check and only call `setServerStatus("ok")` when the status is not 502/503/504.
4. Run `npx vitest run ServerStatusBanner` again, then `npx vitest run` and `npm run lint`.

**Result:**
- Before the fix: 8 passed, 1 failed (the banner disappeared after a 503).
- After the fix: 9 of 9 passed in `ServerStatusBanner`.
- Full run: 46 files, 328 tests passed.
- A local `curl` with an `Origin` header showed that our backend sends CORS headers even on a 401, so a normal request can read its answer.
- Not yet tested: that Render's real error page has no CORS headers (so `fetch` throws). This is checked on the deployed app after it has slept.

**Conclusion:** The test reproduces the bug and the fix removes it. "Try again" still uses `no-cors` and is the next step.

## Step 6: "Try again" no longer trusts a host error page

**What and why:** "Try again" used `mode: "no-cors"` like the background check did, so a 502/503/504 from the host counted as an answer and the page was reloaded into the same error. It is now a normal CORS request with the same rule as the background check: it must succeed and the status must not be 502/503/504, otherwise "Fortfarande ingen kontakt" is shown. We tested that a 503 after clicking "Försök igen" shows that text.

**How:**
1. Add the test "'Try again' says so when the host answers 503 (#254)": `fetch` answers 503, the button is clicked, the text must appear.
2. Run `npx vitest run ServerStatusBanner` before the fix. The new test must fail.
3. In `handleTryAgain`, remove `mode: "no-cors"` and throw on 502/503/504 so the existing `catch` handles it. Update the comment above it.
4. Run `npx vitest run ServerStatusBanner` again, then `npx vitest run` and `npm run lint`.

**Result:**
- Before the fix: 9 passed, 1 failed (no "Fortfarande ingen kontakt" after a 503).
- After the fix: 10 of 10 passed in `ServerStatusBanner`.
- First full run: 9 tests in 6 other files timed out (5 s) while the computer was busy (128 s instead of about 75 s). Run alone they passed (8 files, 54 tests). A second full run was green.
- Full run: 46 files, 329 tests passed.
- Not yet tested in the running app or on the deployed app (see the manual tests for #254).

**Conclusion:** The test reproduces the bug and the fix removes it. Both the background check and "Try again" now ignore the host's error page. Remaining: manual test in the running app, and on the deployed app after Render has slept.
