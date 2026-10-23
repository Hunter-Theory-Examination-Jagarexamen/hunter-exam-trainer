# Meeting: MVP status, deployment and tester feedback

- **Date:** 2026-10-22, 13:00
- **Attendees:** Fadi Alaraj, Md Abdus Sikdar, Maja Blom, Hans Malmefjäll

## Topics discussed

### MVP status before 2026-10-29

Henric's new student group starts testing the app next week. The group went
through the open work that affects the Minimum Viable Product:

- #123 Admin start page with an overview (Fadi).
- #116 Mock exam answers can be lost when the submit fails (Sikdar).
- Bug fixes, such as the bug that blocks having more than one admin (#139).
- App language: Swedish is a high priority for Klas, but there is no issue for it yet.

### Deployment

The real deployment solution has not arrived. The temporary deployment
(managed by Sikdar) is the only option for the testing week, unless Allez
provides the official deployment before then. Known problems and possible fixes:

- Render sleeps after about 15 minutes, so the first request takes 30–60 seconds.
  Possible fix: a pinger, and a "The server is starting, this can take up to a
  minute" message in the app.
- Every push to `develop` deploys directly, with no test gate.
  Possible fix: deploy from `main` instead of `develop`.
- The deployed app uses the same database as local development, so test
  students' data mixes with our dev data, and a local reset would wipe it.
  Possible fix: a separate database for the deployment. This matters most.

### Practical details

- How to collect the testers' feedback (form, Henric, or issues), see #39.
- GDPR and data storage.
- Google login stays hidden in the interface until Allez decides they want it (#61).
- App language (Q-010 in Clarifications).

## Decisions made

1. Not final: the "report a question" feature (#140) could be expanded to
   general feedback, so test students can send feedback from the app. The
   group approved this as a possible solution for collecting tester feedback.

## Follow-up work

- Implement the deployment fixes above.
