# Test: account deletion follow-ups

- **Date:** 2026-11-26
- **Branch / commit:** `feature/243-account-deletion-followups`
- **Related issues:** #243 (follow-up to #194)

## What we tested and why

Closes the loose ends left by #194 (self-delete + admin-delete flows):

1. The confirmation banner after self-delete was in `sv.json` / `en.json`
   but never shown, because `/logout` dropped the `?deleted=1` parameter.
2. Delete errors ("wrong password", "last admin") were shown as raw English
   on the Swedish page.
3. A user deleted by an admin while logged in saw the "session expired"
   message on their next request, which is misleading.
4. Two admins deleting themselves simultaneously could both pass the
   "last admin" check and leave the app without any admin.
5. The Danger zone form did not use the shared change-password form styles.
6. The API specification was missing the error responses for both delete
   endpoints.
7. `AccountDeletionService` had comments that did not match the code
   (referenced `BadCredentialsException` but threw `IllegalArgumentException`).
8. The test suite did not verify that `attempt_questions` rows are deleted
   (required by #194's "all the user's data is gone").

## How

### Automated

From `backend/`:

```bash
./mvnw test
```

From `frontend/`:

```bash
npx tsc -b
npx vitest run
```

### Manual

Started the full stack locally (`./mvnw spring-boot:run` + `npm run dev`)
and tested each scenario below. The admin used
`admin@example.test` / `test-only-password`; a test student was created via
the register page.

1. **Self-delete confirmation banner:**
   Logged in as the student → Profile → Danger zone → Delete account →
   entered correct password → submitted. Expected: redirected to
   `/login?deleted=1`, banner "Ditt konto har tagits bort" visible.

2. **Localized wrong-password message:**
   Repeated the delete with a wrong password. Expected: red message
   "Fel lösenord. Försök igen." (Swedish page).

3. **Last-admin guard:**
   As the only admin (with the seeded student account deleted first),
   Profile → Delete account → correct password. Expected: red message
   "Du är den sista administratören och kan inte ta bort ditt konto."

4. **Deleted-while-logged-in message:**
   Opened two browser sessions: one as a student, one as an admin.
   From the admin session, deleted the student from Elever → name →
   Danger zone. Then clicked anything in the student session.
   Expected: redirected to `/login?deleted=1` (not `?expired=1`).

5. **Real session expiry:**
   Logged in as a different student, waited past the 1-hour token lifetime
   without any request, then clicked anything. Expected: redirected to
   `/login?expired=1` with "Din session har gått ut".

6. **Danger zone styling:**
   Opened Profile → Danger zone → clicked "Ta bort konto". Expected: the
   password field and the Cancel / Delete buttons match the change-password
   form above. No stray `.btn-danger` look-alike styles.

## Result

### Automated

- **Backend:** `./mvnw test` BUILD SUCCESS. All test classes pass,
  including:
    - `AccountDeletionServiceTest` 13/13, including the new
      `deleteOwnAccount_alsoDeletesTheUsersAttemptQuestions` test
    - `PasswordResetControllerTests` 7/7 (verifies `@WebMvcTest` slices
      still load with the new `ApiErrorAuthenticationEntryPoint`, which is
      instantiated inline in `SecurityConfig` to avoid the slice needing a
      bean it can't see)
    - `DeletedUserTokenTest` 1/1
- **Frontend:** `npx tsc -b` clean. `npx vitest run` — all pass, including:
    - `Login.test.tsx` (new) 3 tests: `?deleted=1` shows the deleted
      banner, `?expired=1` shows the expired banner, no query shows nothing
    - `DangerZone.test.tsx` new test: `WRONG_PASSWORD` code from backend
      shows `errors.WRONG_PASSWORD`

### Manual

All six scenarios passed as described. No screenshots the scenarios are
reproducible with the steps above and the expected messages are listed in
the "How" section.

## Conclusion

The #194 loose ends are closed:

- **Same success message for self-delete and admin-delete** a deleted
  user doesn't need to know who triggered it.
- **Localized errors** for the two delete-related failures
  (wrong password, last admin), plus a translated "account deleted"
  notice. Other endpoints keep the existing English message shape; a
  general error-code refactor is filed separately (#242).
- **Admin race** is prevented by locking all admin rows via
  `findByRoleForUpdate(Role.ADMIN)` before counting. Not tested with
  threads a reliable threaded test flakes on CI — but the lock is a
  plain `@Lock(PESSIMISTIC_WRITE)` and the sequential happy path
  (`deleteOwnAccount_whenNotLastAdmin_succeeds`) is covered.
- **Danger zone form** now reuses the shared `.change-password-form`
  classes from Profile, so it looks consistent with the adjacent form.

No regressions in the rest of the backend or frontend test suites.
Ready for the Friday release test.
