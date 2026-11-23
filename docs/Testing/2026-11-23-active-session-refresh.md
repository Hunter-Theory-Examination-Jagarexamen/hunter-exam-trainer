# Test: Active session refresh

- **Date:** 2026-11-23
- **Branch / commit:** `feature/210-active-session-refresh`
- **Related issues:** #210

## What we tested and why

Students were being logged out mid-mock-exam because the JWT was issued once
at login and never renewed. A student who logged in more than 1 hour before
starting a 60-minute exam ran out of token time halfway through.

The fix should:
- keep an **active** student logged in, including across a whole 60-minute
  exam started at any time after logging in,
- still log out a student who has been **inactive** for a long time
  (we picked 1 hour same threshold as today, so no surprise for users).

## How

### Backend tests

1. Added `JwtServiceRefreshTests` (3 tests, unit):
    - fresh token (5 min old) is **not** refreshed,
    - stale token (40 min old) **is** refreshed,
    - token without `iat` is not refreshed (defensive).
2. Added `ActiveSessionRefreshIntegrationTest` (1 test, `@SpringBootTest`):
    - a fresh token does **not** trigger the `X-Refreshed-Token` response
      header. This proves we don't churn a new token on every request.

### Frontend tests

3. Added 2 tests to `apiClient.test.ts`:
    - stores a refreshed token from `X-Refreshed-Token`,
    - keeps the existing token when no header is returned.

### Automated run

```bash
./mvnw test -Dtest="JwtServiceRefreshTests,ActiveSessionRefreshIntegrationTest"
npx vitest run src/__tests__/api/apiClient.test.ts
