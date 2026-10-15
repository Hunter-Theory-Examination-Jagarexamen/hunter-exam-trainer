# Clarifications

This document contains questions that require clarification before or during development.

## Authentication

### Q-001

**Question**

Should users be allowed to practice without logging in (Guest Mode)?

**Reason**

Statistics and progress tracking require user authentication.

**Answer**

No, login required.

---

## Exam Simulation

### Q-002

**Question**

Should every simulated exam contain exactly 70 questions?

**Reason**

This determines the exam generation logic.

**Answer**

Yes.

---

### Q-003

**Question**

Can users return to previous questions during the exam?

**Reason**

This affects the exam navigation and user interface.

**Answer**

Yes.

---

### Q-004

**Question**

Do we need to add timer for the quiz?

**Answer**

Yes, 60 minutes.

---

## Results

### Q-005

**Question**

Should correct answers be displayed immediately after completing the exam, or only after the user finishes reviewing the results?

**Reason**

This affects the result screen design.

**Answer**

Correct answers can be displayed immediately in the practice mode.

---

## Statistics

### Q-006

**Question**

Should instructors be able to view statistics for individual students or only overall group statistics?

**Reason**

This determines the permissions required for the administrator dashboard.

**Answer**

Per-student statistics are fine (Klas, 2026-10-13), with these conditions and additions:

- Students choose which of their results to share with course leaders (high priority).
- Search for a student or subject in the admin area (if we have time).
- A group view needs students grouped by course or year (if we have time).
- How long student data may be stored is still open: see Q-011.

---

## Question Bank

### Q-007

**Question**

Will the provided question bank include images, or will it contain text-only questions?

**Reason**

Image-based questions require additional support in the database and user interface.

**Answer**

Yes (confirmed by Klas, 2026-09-30).

---

### Q-008

**Question**

How many questions should the question bank have? (The Project Plan said 150–200.)

**Reason**

The number affected the plan and what counts as done for the question bank.

**Answer**

No fixed target; admins can add questions in the app (Klas, 2026-10-13).

---

## Login

### Q-009

**Question**

Does Allez want Google login? (Not in the Project Plan; the previous team started it, and it is in the app but switched off.)

**Reason**

To work for real it needs a Google Cloud account, ideally owned by Allez, and the final web address.

**Answer**

_Pending: Klas asks Allez (2026-10-13)._

---

## Language

### Q-010

**Question**

Should both the app language (menus, buttons) and the questions be offered in Swedish and English?

**Reason**

The questions are in Swedish and the app is in English; English as an extra language is "if we have time" (2026-10-13).

**Answer**

_To ask Klas on 2026-10-16._

---

## Privacy (GDPR)

### Q-011

**Question**

How long may the app store students' information (accounts, results)?

**Reason**

Affects what the app keeps, for how long, and what course leaders can see.

**Answer**

_Pending: Klas researches (2026-10-13)._

---

## Scope

### Q-012

**Question**

Are payment/course booking and the practical parts (shooting test, search test) wanted after the MVP?

**Reason**

Both are out of scope for the MVP in the Project Plan; Klas did not answer on 2026-10-13.

**Answer**

_To ask Klas on 2026-10-16._

---

### Q-013

**Question**

Who pays for, and owns, the accounts that cost money: e.g. Apple Developer account (about $99/year) and Google Play developer account (about $25 once) for a native app, hosting for the real deployment, and any paid build service for iPhone builds?

**Reason**

A native app is "big priority if we have time" (2026-10-13). The app can be free for users, but publishing needs paid developer accounts, which should be owned by Allez so they outlive the internship.

**Answer**

_To ask Klas on 2026-10-16._

---
