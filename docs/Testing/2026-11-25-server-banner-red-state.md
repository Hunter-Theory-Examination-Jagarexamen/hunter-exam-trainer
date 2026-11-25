# Test: Server banner stays red after failed requests (#254, step 2)

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
