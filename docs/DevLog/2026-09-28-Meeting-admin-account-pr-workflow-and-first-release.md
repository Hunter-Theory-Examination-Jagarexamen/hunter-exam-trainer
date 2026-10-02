# Meeting: Admin account, PR workflow and first release

- **Date:** 2026-09-28, 14:00
- **Attendees:** Md Abdus Sikdar, Hans Malmefjäll

## Topics discussed

Six proposals, sent to all group members before the meeting and voted on one by one:

1. Admin account creation
2. Shared branches only get updated through PRs
3. Definition of Done
4. Delete branches after merge
5. Presenting the app and code tests to Klas
6. Our first merge to `main`

## Decisions made

All six proposals were approved.

1. **Admin account creation:** we keep what we have: the backend creates one admin at startup from the `ADMIN_EMAIL` / `ADMIN_PASSWORD` environment variables, so no password is stored in Git. Later, the admin panel gets a "make admin" action so an admin can promote other users. Two improvements to the startup code:
   - Check whether *any* admin exists instead of matching the exact email, so changing the email doesn't create a second admin.
   - If the variables are missing, skip creating the admin and log a warning instead of stopping the app.
2. **Shared branches only get updated through PRs:** every change to `develop` and `main` goes through a pull request, even a one-line docs fix. Commits happen on your own branch. This is how most production teams work, and it means there's no judgment call about what counts as "small". Klas has asked us to use the APL to practice working as in a real job.
3. **Definition of Done:** before a PR is merged, the author has:
   - tested the change in the running app, not just the endpoint,
   - updated the README if setup changed (e.g. new environment variables),
   - mentioned database changes that affect existing local databases,
   - written a short "How I tested" note in the PR.
4. **Delete branches after merge:** everyone deletes their own branches after merging them into `develop` (GitHub offers this when the PR is merged). Don't delete other developers' branches without asking them first.
5. **Presenting to Klas:** we present the app and the code tests from the first week at the meeting with Klas on Wednesday 2026-09-30, 10:00.
6. **First merge to `main`:** we merge the first weeks' work into `main` (the production branch) once testing is finished.

## Follow-up work

- Admin startup improvements (any-admin check, warning instead of crash): no issue yet
- "Make admin" action in the admin panel: part of the admin panel work
- Write the Definition of Done into the contributing guide or a PR template: no issue yet
- Presentation for Klas, 2026-09-30 10:00
- Merge `develop` into `main` after testing
