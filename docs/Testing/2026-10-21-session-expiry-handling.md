# Test: Session expiry handling

- **Date:** 2026-10-21
- **Branch / commit:** `fix/87-expired-jwt-handling`
- **Related issues:** #87

## What we tested and why

Verified that when a user's JWT expires while they are logged in, the
frontend:

1. Detects the expired token (via a 401 from any authenticated API call)
2. Clears the token from `localStorage`
3. Redirects to `/login?expired=1`
4. Shows a "session expired" message on the login page

The fix must **not**:
- Redirect on a 401 from `/api/auth/login` (wrong credentials)
- Redirect on a 403 (logged in but not authorized)
- Redirect-loop when already on `/login`

## How

### Automated

New tests in `frontend/src/__tests__/api/apiClient.test.ts`:
- 401 on authenticated endpoint → token cleared, redirect to `/login?expired=1`
- 401 on `/api/auth/login` → token kept, no redirect
- 403 on any endpoint → token kept, no redirect
- 401 while already on `/login` → token cleared, no redirect (avoids loop)

Run: `npx vitest run src/__tests__/api/apiClient.test.ts`

### Manual

**Setup:** Log in normally.

**Test with an expired/invalid token:**
1. Open DevTools → Application → Local Storage → `http://localhost:5173`
2. Find `token` and replace its value with `expired-token`
3. Click any nav link that triggers an API call (e.g., Dashboard, Practice, Statistics)
4. **Expected:** redirected to `/login?expired=1`, "Your session has expired. Please log in again." banner shown, token cleared

**Test that login 401 does NOT redirect:**
1. On the login page, enter an email/password that does not exist
2. **Expected:** error message shown ("Invalid email or password"), stays on `/login`, no "session expired" banner

**Test that 403 does NOT redirect:**
1. Log in as a STUDENT user
2. Manually navigate to `/admin/learners` (an admin-only page)
3. **Expected:** API returns 403, admin-only page shows error (or the AdminRoute guard redirects to dashboard). **No** "session expired" banner, **no** logout.

## Result

All checks passed. 

## Conclusion

The frontend now detects expired sessions and redirects to login with a
clear message. Auth endpoints and 403 responses are correctly exempted.

## Notes

- Reactive approach only: the fix intercepts 401 responses as they happen.
  A proactive approach (decode JWT `exp` on an interval) is possible but
  not required — this reactive fix covers the practical UX.
- Mock exam context (#96, #121): if a token expires mid-exam, the submit
  call now fails with a clear redirect to login. Answers are still lost
  — that's a separate issue (#116).
- `frontend/src/services/api.ts` (`apiRequest`) is unused and will be
  removed. The fix lives only in `apiClient.ts`.