# Meeting: Swedish texts, answers as letters, practice refactor

- **Date:** 2026-10-29, 13:00
- **Attendees:** Fadi Alaraj, Md Abdus Sikdar, Hans Malmefjäll
- **Absent:** Maja Blom

## Topics discussed

### Texts in Swedish (#161, Maja)

Klas has given Swedish high priority. The app's texts now live in translation
files: `frontend/src/i18n/locales/sv.json` (Swedish) and `en.json` (English),
with the same keys, grouped by area (`common`, `auth`, `errors`, `practice`,
...). A component uses `const { t } = useTranslation();` and then, for
example, `t("practice.correct")`. The English texts from the previous team are
kept in `en.json`, ready for a language switch later. Tests look for the
Swedish text. After pulling, run `npm install` once (two new libraries).

### Correct answer stored as a letter (#155, Sikdar)

The correct answer used to be stored as the option's text. That broke
questions when an admin edited an option's text, made two options with the
same text both count as correct, and had already broken 5 of the 100 seeded
questions (they always counted as wrong). Practice and the mock exam also
compared texts in their own ways. Now the answer is stored as a letter (A–D)
and both compare one letter.

A fresh database is fine (`questions.json` already has letters). An existing
database must be converted once with SQL commands; the steps were shared with
the group. Sikdar and Hans confirmed that they had converted their local
databases.

### Practice refactor (#168, #170, Hans)

Following the decision to store each answer (DevLog 2026-10-27):

- Every practice answer is stored and checked on the server as it is given
  (#168); the practice page uses it (#170, merged 2026-10-29). Students can
  no longer see the correct answers before answering, and nothing is lost when
  a round is left halfway.
- Quitting asks first and shows "answered X of Y". Skipped questions don't
  count as wrong in practice (they still do in the mock exam, like the real
  exam).
- `GET /api/questions` is removed: admins use `GET /api/admin/questions`, and
  students get questions only through practice rounds and the mock exam.
- The app uses a "data router" (`createBrowserRouter`); new routes still go in
  `routes/routes.tsx`.
- Next: the mock exam stores its answers the same way, then the statistics read
  the stored answers. Random Practice (#151) can start.

### AI in the app

Fadi reminded the group about Christian's AI chat idea from the meeting with
Allez (2026-10-27).

## Decisions made

1. New texts go in the translation files, not in the components.
2. The backend stays in English; the frontend shows Swedish, also for error
   messages from the backend.
3. Larger changes in `ExamService`, `ExamController`, `StatisticsService`,
   `AdminService` and `DashboardService` are checked with Hans first, while the
   refactor issues change them.

## Follow-up work

- Everyone with an existing local database: convert it to answer letters
  (#155).
- Everyone: think about what AI could add to the app, and compare notes at the
  next meeting.
