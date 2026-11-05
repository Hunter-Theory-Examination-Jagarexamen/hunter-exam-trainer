# Test: Forgot-password initial error fix

- **Date:** 2026-10-14
- **Branch / commit:** `fix/107-forgot-password-initial-error`
- **Related issues:** #107

## What we tested and why

Verified that opening `/forgot-password` no longer shows a red "ACC"
error on load, and that error messages still appear when a reset
request fails.

## How

Automated: `ForgotPassword.test.tsx` (3 tests) covering initial state,
error state, and success state.

Manual:
1. Started frontend locally.
2. Opened `/forgot-password` — confirmed no error banner.
3. Submitted a valid email — confirmed success message appears.
4. Submitted with the backend offline — confirmed error message appears.

## Result

All checks passed. Full frontend suite: 97/97 passing.

## Conclusion

The regression is fixed and covered by a test.