# Test: Mock exam excludes the "Introduktion" subject

- **Date:** 2026-11-18
- **Branch / commit:** `feature/221-exclude-introduktion-from-exam`
- **Related issues:** #221

## What we tested and why

The mock exam should simulate the real theory exam and must not include
questions from the "Introduktion" subject (those introduce the course and
aren't on the real exam). Practice by subject and Random Practice must still
offer "Introduktion" questions. The mock exam must still have 70 questions
and still give a clear error (`409 Not enough questions`) if fewer than 70
valid questions remain after filtering.

## How

1. Added two new tests to `ExamServiceTests.java`:
    - `startExamNeverPicksQuestionsFromTheIntroduktionSubject`- seeds a bank
      of 7 "Introduktion" + 93 "Ekologi" questions, starts an exam, asserts
      every one of the 70 chosen questions is from "Ekologi".
    - `startExamFailsClearlyWhenTooFewNonIntroduktionQuestionsRemain`- seeds
      50 "Introduktion" + 50 "Ekologi" (100 total, but only 50 valid after
      filtering), asserts `409 ConflictException` is thrown and no attempt
      is saved.
2. Ran the full `ExamServiceTests` suite (43 tests) and
   `PracticeAttemptServiceTests` (22 tests) with:
   `./mvnw test -Dtest="ExamServiceTests,PracticeAttemptServiceTests"`
3. Manual check in the running app (after merge to `develop`):
    - Started a mock exam and verified no question belongs to "Introduktion".
    - Opened Practice by subject → "Introduktion" still shows its questions.
    - Opened Random Practice → "Introduktion" questions can still appear.
    - Verified the mock exam still has exactly 70 questions.

## Result

- `ExamServiceTests`: **43/43 pass** (41 existing + 2 new).
- `PracticeAttemptServiceTests`: **22/22 pass** (unchanged practice
  behaviour is intentionally not filtered).
- Manual check (planned after merge): see the checkbox list below.

## Conclusion

The mock exam now excludes "Introduktion" as required. Practice rounds keep
offering "Introduktion" questions, unchanged. The 70-question rule and the
`409 Not enough questions` error behave as before, now checked against the
*filtered* bank. No frontend change was needed.

**Follow-ups (filed separately):**
- Replace the hardcoded subject name with an `includedInMockExam` flag on
  `Subject` so admins can exclude other subjects (e.g. "Repetition") without
  a code change, and so renaming "Introduktion" doesn't silently break the
  exclusion.

## Manual verification

- [ ] Mock exam in the running app contains no "Introduktion" questions
- [ ] Practice by subject → "Introduktion" still lists its questions
- [ ] Random Practice → "Introduktion" questions can still appear
- [ ] Mock exam still has exactly 70 questions
- [ ] With too few non-Introduktion questions (test DB), starting an exam
  returns `409 Not enough questions available to start the exam.`
