**PROJECT PLAN**

**The Jägarexamen trainer**

A practice app for theoretical training before the hunter's exam knowledge test

Project form: APL project (workplace-based learning)

Project group: 3 interns · Internship duration: 12 weeks

Date: 2026-06-07

*Prepared by Claude on behalf of Klas Åkerskog*

## 1. Background and purpose

The Jägerexamen's theoretical knowledge test includes 70 multiple-choice questions in eleven subject areas (hunting ethics and wildlife conservation, ecology, species knowledge for mammals and birds, weapons and shooting, hunting dogs and hunting methods, search, hunting legislation, etc.), and requires at least 60 correct (about 86 percent) for a passing result. Many who take a hunting degree course find that the large amount of detailed knowledge is difficult to repeat between course sessions.

The purpose of this project is to develop a digital practice app where participants can practice the questions of the theory test at their own pace - on computer or mobile - and receive direct feedback, statistics on their strong and weak areas and the opportunity to simulate a full test. The app must be able to be used as a supplement to regular hunting degree courses, for example those described in the syllabuses for Stora Tollabo and Åsen's farm.

## 2. Objectives and delimitation

**2.1 Project objectives**

  - Deliver a working, tested application (web + mobile-adapted PWA) that covers all eleven subject areas in the theory exam.

  - The app must have at least three practice modes: free practice per subject area, mixed random mode and a test simulation with 70 questions and timing.

  - Users must be able to see their history and statistics (accuracy per subject area, development over time).

  - Provide a simple admin interface where questions can be added, edited and categorized.

  - Document the system (requirement specification, system sketch, test results and commissioning guide) so that it can be managed further after the internship period.

**2.2 Delimitations (out of scope for MVP)**

  - Built-in payment function or course booking is not handled in this version.

  - Practical elements (shooting test, search test) are not included - the app only trains the theoretical part.

  - Full native mobile app (App Store/Google Play) is not prioritized; an installable web app (PWA) is considered to provide the best benefit in relation to time and resources.

## 3. Target group and benefit

The primary target group is people studying for the hunting degree, either on their own or via a course (for example at Stora Tollabo or a study circle at Åsen's farm). Secondary target group are course leaders/instructors, who via the admin interface can follow the participants' results and adapt the teaching to the areas where the group performs the weakest.

## 4. Project organization and roles

The group consists of three interns who work together throughout the project, but with clear main responsibilities to facilitate planning and follow-up. The roles do not rotate during the project, but everyone participates in joint requirements work, design and testing.

|                            |                                      |                                                                           |
| -------------------------- | ------------------------------------ | ------------------------------------------------------------------------- |
| **Role**                   | **Main responsibility**              | **Examples of tasks**                                                     |
| Intern 1 – Frontend        | User Interface and PWA               | Quiz Views, Exam Simulation, Statistics Pages, Mobile Adaptation          |
| Intern 2 – Backend         | API, database and authentication     | Data model, query logic, correction, login, operation                     |
| Intern 3 – Content/Test/UX | Question bank, quality and usability | Compile and categorize questions, admin interface, testing, documentation |

## 5. Working method

The project is run according to a simple agile working method with weekly sprints:

  - Every Monday: short planning meeting - what needs to be done this week, and who does what.

  - Daily: 10–15 minute reconciliation (what is ready, what is in progress, are there obstacles).

  - Every Friday: demo of what's finished + updating a simple task board (eg Trello or GitHub Projects).

  - All source code work takes place in a shared version-controlled repo (Git) with clear commit messages and pull requests that are reviewed by a colleague before being merged.

## 6. Timetable – 12 weeks

The schedule is divided into four phases. It is deliberately laid out so that a first, simple version of the app is ready already in the middle of the internship (week 6) – this reduces risk and gives time for adjustments based on tests with real users.

|          |                                            |                                                                                                                                                                                                                                                   |
| -------- | ------------------------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Week** | **Phase / focus**                          | **Activities and deliverables**                                                                                                                                                                                                                   |
| 1–2      | Preliminary study and planning             | Gathering requirements (go through the Jägareförbundet's course material and the test's question types), sketch interfaces, choose technology, set up project environment and Git-repo. Deliverable: requirement specification and system sketch. |
| 3–4      | Basic structure                            | Build data model and database, set up API skeleton, first interface sketches in code, start collecting and categorizing questions for the question bank (goal: at least 150 questions distributed across all subject areas).                      |
| 5–6      | First Working Version (MVP)                | Quiz mode with correction and results, easy login, basic statistics. Deliverable: internal demo and initial coordination with supervisor/test user.                                                                                               |
| 7–9      | Further development                        | Exam simulation (70 questions, timed), in-depth statistics and progression over time, admin interface to manage the question bank, mobile customization (PWA, installable).                                                                       |
| 10       | User tests                                 | Have a test group (eg participants in an ongoing hunter exam course) try out the app, collect feedback and bug reports.                                                                                                                           |
| 11       | Adjustments and quality assurance          | Fixed bugs and comments from the test round, fine-tuned interface, writing and content review of all questions in the question bank.                                                                                                              |
| 12       | Commissioning, documentation and reporting | Put the app into operation, complete documentation (operation, management, getting started guide), prepare and hold the final presentation/report of the project.                                                                                 |

## 7. Deliverables

  - Working application (web and mobile-friendly PWA) with question bank, practice modes, exam simulation and statistics.

  - Admin interface for managing questions and following up on results.

  - Question bank with at least 150–200 categorized and quality-reviewed questions.

  - Technical documentation: requirements specification, system sketch (see section 9), test report and operation/management guide.

  - Final report/presentation of the project and its results.

## 8. Risks and measures

|                                                                 |                            |                                                                                                                                                         |
| --------------------------------------------------------------- | -------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Risk**                                                        | **Likelihood/Consequence** | **Preventive measure**                                                                                                                                  |
| Time constraints – 12 weeks is short for a complete app         | High / High                | Clear delineation to an MVP (Section 2.2), first delivery as early as week 6, prioritize core functions before extra functions.                         |
| The question bank becomes too small or contains errors          | Medium / High              | Set aside regular time each week for question collection, have a course leader/instructor review the content before launch.                             |
| Technical knowledge gaps in the group                           | Average / Average          | Choose proven and well-documented technology (Section 10), schedule tutoring/mentor time, share knowledge internally via pair programming.              |
| Low participation in user testing                               | Average / Average          | Book the test session early with a course that is already in progress (e.g. Stora Tollabo or Åsens gård) and offer the testers early access to the app. |
| Operation and management after the end of the internship period | Average / Average          | Choose a simple, cost-effective operating environment and write clear documentation so that the app can be taken over by someone else.                  |

## 9. System sketch

Below is a general sketch of the main parts of the system and how they work together. More detailed technical documentation is produced during the preliminary study phase (weeks 1–2).

![System sketch](media/media/image1.png)

**This is how the system works in brief**

• Client (frontend): a web app built as a Progressive Web App (PWA) – the same codebase works in the browser on a computer and can be installed as an app on a mobile/tablet, saving time compared to building separate native apps.

• Backend/API: handles login, randomizes questions, corrects answers and calculates results and statistics. Communicates with the client via a simple REST API (JSON over HTTPS).

• Database: stores questions and answer options (categorized according to the exam's eleven subject areas), user accounts, and exam results and answer history that form the basis of the statistics.

• Admin interface: a separate mode where course leaders can add and edit questions and monitor how a group is performing - built on top of the same backend.

## 10. Proposal for technology choice

The technology choices below are suggestions that take into account that the group is relatively new to the professional role: they are well documented, have a large community and allow the same skills (JavaScript/TypeScript) to be used throughout the stack, which facilitates collaboration within a small group.

|                        |                                                            |                                                                                                                                           |
| ---------------------- | ---------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------- |
| **Part of the system** | **Proposal**                                               | **Justification**                                                                                                                         |
| Front end              | React or Next.js, built as a PWA                           | Wide range of learning resources, reusable components, can be installed as an app on the mobile without separate native development.      |
| Backend/API            | Node.js with Express (REST API)                            | Same language (JavaScript/TypeScript) as frontend, easy to get started with, well documented.                                             |
| Database               | PostgreSQL (eg via Supabase) or Firebase                   | Ready-made cloud services with built-in authentication and low threshold for operation, suitable for a smaller project with limited time. |
| Authentication         | Email/password or ready login service (e.g. Supabase Auth) | Avoid building safety-critical functionality from scratch - reduces risk and saves time.                                                  |
| Version management     | Git (eg GitHub)                                            | Steep learning curve is low, industry standard, supports group code review.                                                               |

*Final technique selection is made jointly by the group in weeks 1–2, taking into account what skills are already in the group and what support can be obtained via supervisors or school.*

## 11. Follow-up and approval

The project's progress is reconciled every week according to the working method in section 5. At each phase transition (weeks 2, 6, 9 and 12 according to the schedule) a slightly more formal reconciliation is done with supervisors where deliverables for the phase are reviewed and any adjustments to the plan are jointly decided.
