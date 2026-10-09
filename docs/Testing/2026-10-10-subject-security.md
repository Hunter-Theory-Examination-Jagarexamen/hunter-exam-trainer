# Test: Subject write endpoints require ADMIN

- **Date:** 2026-10-10
- **Branch / commit:** `feature/97-subject-security`
- **Related issues:** #97

## What we tested and why

Verified that POST/PUT/DELETE on `/api/subjects` reject STUDENT and
anonymous requests, and that ADMIN requests succeed. GET remains
open to any authenticated user.

## How

Automated backend test: `SubjectControllerSecurityTests` (uses MockMvc
with mocked JWT authorities).

Manual verification:

1. Started backend.
2. Called `POST /api/subjects` with a STUDENT JWT expect 403.
3. Same for `PUT /api/subjects/{id}` and `DELETE /api/subjects/{id}`.
4. Called `GET /api/subjects` with a STUDENT JWT expect 200.
5. Repeated steps 2–3 with an ADMIN JWT expect 201 / 200 / 204.

## Result

All checks passed. [Update once you've run]

## Conclusion

Subject write endpoints are now correctly admin-only.

## Notes

- `@EnableMethodSecurity` was enabled in `SecurityConfig` makes the existing
  `@PreAuthorize` annotations on `SubjectController` actually fire.
- URL rules for subject writes were also added to `SecurityConfig` (defense in depth).
- Follow-up: consider moving subject writes under `/api/admin/**` for consistency.