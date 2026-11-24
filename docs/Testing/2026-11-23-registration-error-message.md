# Test: registration error messages (#242)

- **Date:** 2026-11-23 (manual tests 2026-11-24)
- **Branch / commit:** fix/242-registration-error-message (73cc8a2)
- **Related issues:** #242

## What we tested and why

That registration shows a clear message when it fails: "already registered" for 409, and a general message for other errors. Before, the page always showed "Ett oväntat fel uppstod."

## How

1. Run `npx vitest run RegisterForm` in `frontend`.
2. Run the full suite with `npx vitest run`.
3. (Manual, see Result) Register in the running app with an email that already exists.
4. Stop the backend and try to register again.

## Result

- RegisterForm: 9 of 9 tests pass, including the changed 409 test and the new 500 test.
- Full suite: 320 of 320 pass.
- Manual test in the running app (2026-11-24): registering with an email that already exists shows "E-postadressen är redan registrerad." and stays on the registration page.
- Manual test with the backend stopped (2026-11-24): registering shows "Det går inte att nå servern. Kontrollera din internetanslutning eller försök igen om en stund." and stays on the registration page. Not covered by an automated test yet.

## Conclusion

Both the 409 case and the server-unreachable case work as intended. A small automated test for the server-unreachable case is a possible follow-up.
