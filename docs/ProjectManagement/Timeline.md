# Project Timeline

The project runs in two 12-week periods: weeks 1–12 were the previous team's, and weeks 13–24 are the current team's APL period (starting week 38, 14 September 2026).

## Weeks 13–24: current team

Status as of 2026-10-08 (`develop` up to PR #95).

| Week | Calendar week | Phase | Deliverables | Status |
|------|---------------|-------|--------------|--------|
| 13 | 38 | Transition & Onboarding | Handover from the previous team, local setup verified (#7), workflow agreed (branches and PRs, DevLog, Project board) | Done |
| 14 | 39 | Database & PWA | PostgreSQL migration (#43, #51), installable PWA (#26), exam time limit (#35), decisions on hosting and authentication; Google login (#69) and password recovery (#80) reviewed and merged | Done |
| 15 | 40 | Exam Fixes & Test Hosting | Working mock exam (#49), error handling (#44), test deployment online (#57), app install tested on a phone (#47), authentication work reviewed and merged into `develop` | Done |
| 16 | 41 | Authentication Extensions | Planned: app registered in Google Cloud Console so Google login works for real (still localhost only, #104), email service connected so password recovery sends real emails (not yet verified on staging). Also done this week, ahead of plan: statistics over time (#34), admin panel (question bank, add/edit/delete questions, learner progress, optional question images), frontend tests (#38) and backend tests (#37) | In Progress |
| 17–18 | 42–43 | Known Problems | Statistics & Progress was done early (week 41). Instead: fix the known problems found in reviews before user testing (#96, #98, #102, #104) | Planned |
| 19–20 | 44–45 | User Testing | Testing with real users, bug reporting, usability feedback | Planned |
| 21–22 | 46–47 | Admin Panel | Mostly done early (week 41). Remaining: admin page for subjects, question images in practice mode and the admin area (#102). Free time can go to optional features agreed with Klas | Planned |
| 23 | 48 | Quality Assurance | Bug fixes, UI improvements, performance optimization, final testing | Planned |
| 24 | 49 | Final Delivery | Project Presentation & Documentation | Planned |

## Weeks 1–12: previous team

The previous team's original plan. Phases marked "Carried over" continue in weeks 13–24.

| Week | Phase                        | Deliverables                                                                                                     | Status       |
|------|------------------------------|------------------------------------------------------------------------------------------------------------------|--------------|
| 1    | Project Setup                | GitHub repository, project structure, documentation setup                                                        | Done         |
| 2    | Study Phase                  | Requirements gathering, SRS draft, UI review                                                                     | Done         |
| 3    | System Design                | ER diagram, API design, database schema                                                                          | Done         |
| 4    | Development Setup            | Backend and frontend initialized, database connected                                                             | Done         |
| 5    | Authentication & Dashboard   | Login, registration, dashboard, navigation                                                                       | Done         |
| 6    | Minimum Viable Product (MVP) | Working quiz system with login, question display, answer validation, score calculation, and basic result history | Done         |
| 7    | Exam Simulation              | Full 70-question mock exam with timer, randomization, automatic submission, and pass/fail evaluation             | Carried over |
| 8    | Statistics & Progress        | Performance dashboard, quiz history, subject-wise accuracy, and progress visualization                           | Carried over |
| 9    | Admin Panel                  | Question management, category management, and instructor dashboard for monitoring learner progress               | Carried over |
| 10   | User Testing                 | Testing with real users, bug reporting, usability feedback                                                       | Carried over |
| 11   | Quality Assurance            | Bug fixes, UI improvements, performance optimization, final testing                                              | Carried over |
| 12   | Final Delivery               | Project Presentation & Documentation                                                                             | Carried over |
