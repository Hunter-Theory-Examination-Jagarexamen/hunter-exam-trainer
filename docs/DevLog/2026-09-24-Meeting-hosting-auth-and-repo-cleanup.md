# Meeting: Hosting, authentication and repo cleanup

- **Date:** 2026-09-24, 13:00
- **Attendees:** Fadi Alaraj, Md Abdus Sikdar, Hans Malmefjäll

## Topics discussed

Six proposals, voted on one by one:

1. Temporary online hosting for testing
2. Security: own Spring Security implementation or Supabase
3. Google login
4. Project Timeline
5. Old branches from the previous team
6. Default branch on GitHub

## Decisions made

All six proposals were approved.

1. **Temporary online hosting:** Sikdar sets up and manages a test deployment (backend, database and frontend). It runs the same code from `develop`, configured through environment variables (possible since #51), so no separate branch is needed. Locally we keep using localhost. Main reason: phones require HTTPS to install the app, which blocks mobile testing (#47).
2. **Security:** we continue with our own Spring Security + JWT implementation instead of switching to Supabase. Login and registration already work, and switching would mean changing user IDs and linked exam data mid-project. Password recovery becomes new work and needs an email service.
3. **Google login:** we keep the feature. The app needs to be registered in Google Cloud Console to get a client ID and secret.
4. **Project Timeline:** `Timeline.md` is updated.
5. **Old branches:** the four branches from the previous team (`dev/gregory`, `dev/paul`, `dev/practice`, `dev/mock`) are deleted. Their content is already in `develop` in newer versions.
6. **Default branch:** `develop` becomes the default branch on GitHub, so PRs target it automatically and "Closes #N" closes issues on merge. `main` stays as it is.

## Follow-up work

- Set up staging deployment on Render + Vercel: #57
- Password recovery with an email service: #62
- Register the app in Google Cloud Console for Google login: #61
- Timeline updated: done in PR #54
- Old branches deleted: done
- Default branch changed to `develop`: done
