# Store each answer instead of only the total per round

- **Date:** 2026-10-27, 11:01 (Teams group chat; confirmed to the group 17:56)
- **Status:** Accepted
- **Related issues:** #151, #141, #168, #170, #174, #180, #183

## Situation

While building Random Practice (#151), Hans found that the app, as inherited
from the previous team, stored only the total score of each practice round and
mock exam, not the individual answers. A quick solution was possible: save one
random round as several separate results, one per subject. But that has
drawbacks:

- The app can't tell that it was one round, so a quiz history (required by the
  SRS) would show one random round as several entries.
- The same problem returns with "Practise weak areas" (#141, which Klas
  liked).
- Mock exam answers don't count in the per-subject statistics, since only the
  total exam score is saved.
- Reviewing earlier quizzes is impossible, since the answers aren't saved.
- The browser gets the correct answer before the student answers (visible in
  the browser's developer tools).

## Options

1. **Quick solution:** keep storing totals; save a random round as one result
   per subject. Fast, but the drawbacks above remain and repeat for later
   features.
2. **Store each answer** (user, question, correct or not, time), and calculate
   results and statistics from the stored answers. Common practice in learning
   apps such as Moodle and Anki. More database rows, but no noticeable
   performance cost at our size. Takes longer: statistics and Learner Progress
   must be rewritten.

## Decision

Option 2: store each answer. Hans first suggested the quick solution for the
user testing, but later the same day confirmed to the group that the better
solution is needed to build the basic features, and took responsibility for
laying the groundwork while few other features were in progress.

## Consequences

- Random Practice works without special handling; each answer knows its
  question's subject.
- Mock exam answers count in the per-subject statistics.
- Quiz history (FR-024) and reviewing earlier quizzes (FR-029) become possible.
- Answers are checked on the server, so the correct answers are no longer sent
  to the browser beforehand. Working on this (2026-10-28) also showed that the
  server saved practice scores sent by the browser unchecked; that is fixed by
  the same change. Lesson for new endpoints: return a DTO with only the fields
  the client needs, and never trust numbers the client calculates.
- The work was split into: #168 (store each practice answer), #170 (practice
  page uses server-checked rounds), #174 (mock exam answers stored the same
  way), #180 (statistics, Dashboard and Learner Progress from the stored
  answers), #183 (remove the old result tables).
