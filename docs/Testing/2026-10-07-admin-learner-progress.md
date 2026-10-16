# Test: Admin learner progress view

- **Date:** 2026-10-07
- **Branch / commit:** `feature/33-admin-learner-progress` (commit: <paste SHA here>)
- **Related issues:** #33

## What we tested and why

Verified that an ADMIN user can view aggregated learner progress (FR-048),
and that STUDENT users cannot access the admin view (NFR-007).

## How

1. Start backend (`./mvnw spring-boot:run`) and frontend (`npm run dev`).
2. Log in as an ADMIN user.
3. Check the sidebar — the "Learner Progress" link should be visible.
4. Click it; the page should load at `/admin/learners` with a list of learners.
5. Log out, log in as a STUDENT user.
6. Check the sidebar — the "Learner Progress" link should NOT be visible.
7. Navigate directly to `/admin/learners` — should redirect to `/dashboard`.
8. Call `GET /api/admin/learners` with a STUDENT token via `curl` — should return 403.

## Result

All checks passed:

- As ADMIN:
    - ✅ Sidebar shows "Learner Progress" link
    - ✅ Clicking the link loads `/admin/learners`
    - ✅ Page displays a list of learners with their subject breakdowns
    - ✅ `GET /api/admin/learners` returns data
- As STUDENT:
    - ✅ Sidebar does not show the "Learner Progress" link
    - ✅ Direct navigation to `/admin/learners` redirects to `/dashboard`
    - ✅ Direct API call to `/api/admin/learners` returns 403 Forbidden

## Conclusion

Feature works as specified. Admin view is functional; student access is properly blocked.

## Notes

- `/api/admin/**` is protected by the existing `hasRole("ADMIN")` rule in `SecurityConfig` — no security config changes needed.
- Frontend fetches the current user's role once via `/api/users/me` in `Layout.tsx` and passes it down — no extra API calls per page load.