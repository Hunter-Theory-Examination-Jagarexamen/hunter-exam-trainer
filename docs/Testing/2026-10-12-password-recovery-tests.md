# Test Log: Frontend tests for Forgot / Reset Password (#91)

**Date:** 2026-10-12
**Scope:** `frontend/src/__tests__/pages/Auth/PasswordRecovery.test.tsx`

## Overview

Added unit and integration tests for `ForgotPassword` and `ResetPassword` pages (#91):
- Mocked `authService` endpoints (`forgotPassword` and `resetPassword`) and verified delegation.
- Verified generic message behaviour on `ForgotPassword` to prevent email enumeration.
- Verified valid 43-character token parsing from `window.location.hash` on `ResetPassword`.
- Validated password match, minimum length (8 characters), and successful reset execution.

## Test Results

```text
 ✓ src/__tests__/pages/Auth/PasswordRecovery.test.tsx (6 tests)
   ✓ Password Recovery Pages (6)
     ✓ ForgotPassword Page (2)
       ✓ sends request link on valid email submission
       ✓ displays error message when backend request fails
     ✓ ResetPassword Page (4)
       ✓ shows error when reset token is missing from URL hash
       ✓ resets password successfully with valid input and token
       ✓ shows error when passwords do not match
       ✓ shows error when password is under 8 characters

 Test Files  10 passed (10)
      Tests  58 passed (58)