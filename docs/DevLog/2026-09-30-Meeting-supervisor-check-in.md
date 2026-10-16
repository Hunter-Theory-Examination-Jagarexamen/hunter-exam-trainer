# Meeting: Supervisor check-in with Klas

- **Date:** 2026-09-30, 10:00
- **Attendees:** Klas Åkerskog, Fadi Alaraj, Md Abdus Sikdar, Maja Blom, Hans Malmefjäll

## Topics discussed

Hans presented the team's progress since week 38 (Postgres, installable PWA,
exam time limit and working mock exam, user roles, temporary deployment, Google
login work brought into `develop`, the new PR workflow) and asked Klas a set of
open questions:

1. Branch protection for `main`: who owns the GitHub account, can `main` be protected?
2. Real deployment after the internship: where does the app run, who pays, who maintains it? User accounts also mean GDPR.
3. Temporary deployment: is it OK to use personal accounts (Sikdar's Neon database)?
4. Guest login: Clarification Q-001 already says "No, login required". Should it be removed?
5. Question bank: who reviews the content, where do the questions come from, and do we need to grow it to 150–200 questions as the plan says?
6. Clarification Q-006: should instructors see statistics per student, or only for the whole group?
7. Clarification Q-007: will questions include images?
8. User testing in weeks 44–45: can Klas help book a course group?

## Decisions made

1. **Branch protection:** the group may make the repository **public**, which
   enables GitHub's branch protection features for `main`.
2. **Real deployment:** no answer yet; Klas will come back with information later.
3. **Temporary deployment:** we may use the temporary deployment (Render, Vercel,
   Neon) freely.
4. **Guest login:** **remove it**, in line with Q-001.
5. **Question bank:** the 150–200 question target is dropped. What matters is that
   **admins can add, edit and remove questions**.
6. **Q-006:** not answered; still open.
7. **Q-007:** **yes**, questions can include images.
8. **User testing:** Klas needs **3 weeks' notice** to arrange a test group.

Nice-to-have feature mentioned: grouping students by course or year.

Not asked: whose accounts to use for Google Cloud Console (#61) and the email
service (#62). We need to research the options before we can explain the question.

## Follow-up work

- Make the repository public and set up branch protection for `main` (require a PR before merging).
- Remove guest login: backend `/api/auth/guest` and `guestLogin()`, the frontend "Continue with Guest" button (from #68).
- Support images in questions (Q-007): data model, admin input, display in practice and exam.
- Admin question management: endpoints exist (PR #71, issue #30); the admin interface is the next step.
- User testing in weeks 44–45: give Klas notice by **2026-10-05** at the latest (3 weeks before week 44).
- Update `Clarifications.md` with the Q-007 answer.
- Research Google Cloud Console and email service account options (#61, #62) before asking Klas.
- Still waiting: real deployment details from Klas; Q-006.
