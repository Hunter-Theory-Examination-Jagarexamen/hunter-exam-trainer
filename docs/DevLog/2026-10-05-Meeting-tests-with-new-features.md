# Meeting: Tests come with new features

- **Date:** 2026-10-05, 14:00
- **Attendees:** Fadi Alaraj, Md Abdus Sikdar, Maja Blom, Hans Malmefjäll

## Topics discussed

The frontend now has a test runner (Vitest + React Testing Library, #38), and
backend tests are in progress (#37). The question was who writes the tests
for new work from now on.

Proposal: the person who adds a feature also adds the tests for that feature,
which is the normal way teams work.

## Decisions made

**Whoever adds a feature also writes the tests for it:** unit/component tests
in the frontend and/or backend, depending on what the feature changes.

## Follow-up work

- Backend tests for existing features: #37 (Hans).
- Add a "Tests added or updated" checkbox to the Definition of Done in
  `.github/pull_request_template.md` (Hans).
