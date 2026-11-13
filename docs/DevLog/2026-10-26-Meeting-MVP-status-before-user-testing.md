# Meeting: MVP status before user testing

- **Date:** 2026-10-26, 14:00
- **Attendees:** Fadi Alaraj, Md Abdus Sikdar, Maja Blom, Hans Malmefjäll

## Topics discussed

### Reminder

Meeting with Klas and Allez (project owner Christian and course leader Henric)
on Tuesday 2026-10-27, 17:00.

### MVP status before user testing

Hans presented what must be done before the user testing starts. Issues marked
"Prio" are the goal to finish before then.

- #116 Mock exam answers can be lost when the submit fails (in progress).
- New issues for the first release:
  - #159 Prio: show a confirmation after creating an account.
  - #156 Prio: Learners page as a list of names with a detail view.
  - Admin can promote a user to admin and demote an admin (after #156).
  - #151 Random Practice.
  - Students can report a problem with a question; may become general tester
    feedback.

### Deployment

How the temporary deployment should work during user testing (Hans and
Sikdar):

- `main` becomes the "production" branch: the deployment builds from `main`,
  and `develop` is merged into `main` only after testing.
- A pinger keeps the free Render backend from sleeping.
- The deployment gets its own database, separate from local development.

### Practical details

- GDPR / data storage: waiting for information from Klas.
- Collecting tester feedback: in the app via #140, a form, or through Henric.
  The developers liked the in-app option; it stays a question for Klas and
  Allez at the meeting on 2026-10-27 (see #39).

### Git setting: `.gitattributes`

Hans proposed a root `.gitattributes` file, so everyone gets the same
line-ending settings in Git. It mostly helps tools that run in a Linux shell,
which otherwise see every file as modified. Nothing changes in the files or in
IntelliJ; a one-time conflict after the merge can be solved with a few git
commands that Hans provides.

## Decisions made

1. The new issues above are created on the Project board.
2. Deployment: `main` as the production branch, a pinger, and a separate
   database for the deployment.
3. The root `.gitattributes` is approved (#154).

## Follow-up work

- Create the new issues (promote/demote, report a question / feedback).
- Implement the deployment decisions.
- Ask Klas and Allez how to collect tester feedback (2026-10-27).
