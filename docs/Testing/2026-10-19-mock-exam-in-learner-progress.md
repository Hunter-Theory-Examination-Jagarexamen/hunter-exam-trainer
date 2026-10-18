# Test: Mock exam results in Learner Progress

- **Date:** 2026-10-19
- **Branch / commit:** `feature/121-mock-exams-in-learner-progress`
- **Related issues:** #121

## What we tested and why

Verified that the admin Learner Progress view (`/admin/learners`) now
shows mock exam results alongside practice results, so a course leader
can see whether a student is ready for the real exam — not just how
they've practiced.

Specifically:

1. A student who has only taken mock exams (no practice) shows their
   exam count, latest/best score, and pass/fail, plus a "Last activity"
   that reflects the exam date.
2. A student who has only practiced (no exams) continues to show
   practice results and "No mock exams yet."
3. A student with both shows combined last activity and both sections.
4. A student with neither shows "Never" for activity and clear empty
   states for both sections.

## How

### Automated

Backend tests (`AdminServiceTests`):
- `learnerWithNoResultsShowsEmptyState`
- `learnerWithOnlyMockExamsShowsExamsAndLastActivity`
- `learnerWithOnlyPracticeShowsPracticeAndLastActivity`
- `learnerWithBothShowsLatestActivity`
- `latestExamBelowPassThresholdFails`

Frontend tests (`LearnerProgress.test.tsx`):
- Same four scenarios at the component level.

Run: