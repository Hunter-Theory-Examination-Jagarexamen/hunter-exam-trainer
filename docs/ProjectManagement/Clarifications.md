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

_To be discussed._

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