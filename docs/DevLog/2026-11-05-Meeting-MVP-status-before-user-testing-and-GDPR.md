# Meeting: MVP status before user testing, deployment and GDPR

- **Date:** 2026-11-05, 13:00
- **Attendees:** Fadi Alaraj, Maja Blom, Hans Malmefjäll
- **Absent:** Md Abdus Sikdar

## Topics discussed

### Reminders

- Meeting with Allez (Klas, project owner Christian and course leader Henric)
  on 2026-11-06, 17:00 (later moved to 2026-11-09).

### Recap of the meeting on 2026-10-29 (for Maja)

- The correct answer is stored as a letter A–D (#155).
- Practice rounds save and check every answer on the server (#168, #170).

### MVP status before the user test on 2026-11-09

Status report:

- Done: mock exam overview and bookmarks (#175), statistics from the stored
  answers and the old result tables removed (#180, #183), Random Practice
  (#151).
- Expected before the test: all app texts in Swedish (#161).
- Open Prio issues: leaving a mock exam without asking (#186), lost answers
  when an exam ends without a result (#187), and the feedback form (#140).

### Deployment

A tested version of `develop` is merged into `main`, and the deployment builds
from `main`, so only tested releases reach the students. After the switch, the
deployed app is tested on a real phone before the user test.

### Practical details

- Tester feedback: the feedback form and "report a question" (#140).
- GDPR: still waiting for Klas. Registration asks students to accept a Terms of
  Use and a Privacy Policy that don't exist yet. Suggestions: a temporary,
  plain-language text drafted with AI (what we store, who sees it, how long,
  who to contact) for Klas to approve, and an EU region for the deployment's
  database.
- Password reset by email works locally but isn't connected to a mail service
  on the deployment.

### Planned features

Hans has a list of enhancements and fixes; after the user test they are
prioritised together with the testers' feedback. The SRS still requires some
features the app doesn't have (e.g. showing whether the mock exam is passed, a
summary of right and wrong answers after the exam, quiz history). It is unclear
whether the SRS is Klas's requirements or the previous team's own document.

### AI chat in the app

Hans presented notes on Christian's AI chat idea: what a free and a paid API
key could do (explain answers, explain terms, advice from the student's
statistics), why it needs a trusted source (course material or public texts),
cost and GDPR, and a possible smallest version ("Explain this answer" in
practice mode).

## Decisions made

1. Do a phone test of the deployed app before the user test.
2. Ask Klas whether the temporary, AI-drafted Terms of Use and Privacy Policy
   are acceptable.
3. Ask Klas whether the SRS is his requirements or the previous team's own
   document.

## Follow-up work

- Hans builds #140 (feedback form and report a question).
- Phone test after the deployment switches to `main`.
- Questions for Klas: the temporary GDPR texts, the SRS.
