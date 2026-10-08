# Test Plan

How we test the Hunter Exam Trainer, and where the results are recorded. The project plan asks for "a working, tested application" and a test report; the SRS lists the Test Plan and Test Report as deliverables.

## Scope

- The functional and non-functional requirements in the [SRS](../Requirements/SRS.md), checked against its acceptance criteria (AC-001 to AC-019).
- The web app in the browser and the installed PWA on a phone.
- The question bank: that questions, answers and subject areas are correct.

Out of scope: load and security testing beyond basic checks, and native app-store apps (not part of the project).

## Test levels

| Level | What | How |
|-------|------|-----|
| Automated, backend | Services and API endpoints | JUnit and Spring Boot Test, run with `./mvnw test` |
| Automated, frontend | The code builds, follows the lint rules, and key components and flows behave correctly | `npm run build`, `npm run lint` and `npx vitest run` (Vitest + React Testing Library, tests in `frontend/src/__tests__/`) |
| Manual, per PR | The changed feature works in the browser, including error cases | The author runs the full stack locally and writes "How I tested" in the PR |
| User testing | Real users try the app and give feedback | Weeks 19–20, see below |

## Environments

- **Local:** backend, frontend and PostgreSQL (Docker), as described in the [README](../../README.md).
- **Test deployment:** the online test environment, running `develop`. Needed for testing on phones, since installing the app requires HTTPS.
- **Browsers:** latest Chrome, Edge, Firefox and Safari (NFR-015), plus at least one Android and one iPhone device.

## User testing

Planned for weeks 19–20 (see the [Timeline](../ProjectManagement/Timeline.md)).

- **Testers:** participants in an ongoing hunter exam course, as the project plan suggests (for example Stora Tollabo or Åsens gård). Book the session early.
- **What they do:** register, practice by subject area, take a full mock exam and look at their statistics, on their own phone or computer.
- **What we collect:** bug reports, usability feedback, and comments on unclear or incorrect questions.
- **After:** bugs and feedback become issues, fixed in the quality assurance week.

## Recording results

Each test run is recorded as its own file in this folder, named `YYYY-MM-DD-short-title.md` (for example `2026-09-18-local-setup-verified.md`). Together these entries form the basis of the Test Report at the end of the project. Add them through a branch and pull request, like any other change.

Format:

```markdown
# Test: short title

- **Date:** YYYY-MM-DD
- **Branch / commit:**
- **Related issues:** #

## What we tested and why

## How

The steps, so someone else can repeat the test.

## Result

What happened?

## Conclusion

What do we do with the result?
```

## Current state and known gaps

- Automated tests: the backend has unit tests for services and web-layer tests for security rules (`backend/src/test/java`); the frontend has unit and component tests (`frontend/src/__tests__/`). The test code itself is the list of what is covered; the test logs in this folder record each round of added tests and their results. No end-to-end tests yet, and the tests are not yet run automatically on pull requests.
- The question bank has 100 questions; the project plan's goal is 150–200 reviewed questions.
