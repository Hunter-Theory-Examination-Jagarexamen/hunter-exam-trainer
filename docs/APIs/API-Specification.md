## API Specification

| Method | Endpoint                      | Purpose                                         | Authentication   |
|--------|-------------------------------|-------------------------------------------------|------------------|
| POST   | /api/auth/register            | Register new user                               | Public           |
| POST   | /api/auth/login               | Authenticate user and return JWT                | Public           |
| GET    | /api/users/me                 | Get logged-in user's profile                    | Required         |
| PUT    | /api/users/me/password        | Change the logged-in user's password            | Required         |
| GET    | /api/subjects                 | Get all subjects and question counts            | Required         |
| POST   | /api/subjects                 | Create a new subject                            | Required (ADMIN) |
| PUT    | /api/subjects/{id}            | Update an existing subject                      | Required (ADMIN) |
| DELETE | /api/subjects/{id}            | Delete a subject                                | Required (ADMIN) |
| GET    | /api/questions?subjectId={id} | Get questions for a specific subject            | Required         |
| POST   | /api/practice/attempts        | Start a practice round for a subject            | Required         |
| POST   | /api/practice/attempts/{id}/answers | Answer one question in a practice round (checked by the server) | Required |
| POST   | /api/practice/attempts/{id}/finish | Finish a practice round and get its totals | Required         |
| POST   | /api/practice/results         | Save a completed practice session result (old; to be replaced by the attempts endpoints) | Required |
| POST   | /api/exam/start               | Start a mock exam and receive 70 questions      | Required         |
| POST   | /api/exam/{sessionId}/answer | Save a single answer on the current exam session | Required |
| POST   | /api/exam/submit              | Submit a mock exam answers and save the result  | Required         |
| GET    | /api/dashboard                | Get dashboard statistics for the logged-in user | Required         |
| GET    | /api/dashboard/recent         | Get the user's recent mock exam activity        | Required         |
| GET    | /api/statistics/subjects      | Get the user's practice performance by subject  | Required         |
| GET    | /api/statistics/progress      | Get the user's weekly accuracy over time        | Required         |
| GET    | /api/admin/learners           | Get aggregated progress for all learners        | Required (ADMIN) |

## Mock exam: request and response

The exam endpoints use a server-side exam session to enforce the time limit (#35).

### `POST /api/exam/start`

No request body. Creates an exam session for the logged-in user and returns 70 random questions.

```json
{
  "sessionId": 12,
  "questions": [
    { "id": 5, "questionText": "...", "optionA": "...", "optionB": "...", "optionC": "...", "optionD": "..." }
  ]
}
```

### `POST /api/exam/submit`

```json
{
  "sessionId": 12,
  "answers": { "5": "A", "17": "C" }
}
```

- `sessionId` is required and must come from `/api/exam/start`.
- `answers` maps a question ID (as a string) to the chosen option letter `A`-`D`. Unanswered questions are left out.
- Rejected if more than 60 minutes have passed since start, or if the session was already submitted.

Response:

```json
{ "totalQuestions": 70, "correctAnswers": 52, "incorrectAnswers": 10, "unanswered": 8, "score": 52 }
```
**Notes:**

- `POST /api/exam/start` links the exam session to the logged-in user. The server records which questions were issued for that session.
- `POST /api/exam/submit` scores against the questions the server issued at `/start`; the client doesn't send a question list. Only the session's owner may submit; other users receive `403 Forbidden`.

**Exams notes:**
- `POST /api/exam/{sessionId}/answer` saves an individual answer as the user gives it. Called by the frontend on each selection. Answers are stored on the session and used at submit time, so a lost connection, expired token, or closed tab does not lose the attempt.

**MockExams notes:**

- `GET /api/admin/learners` returns one entry per learner with:
- `subjectPerformances` — practice results per subject
- `mockExams` — mock exam summary (count, latest/best score, latest pass/fail)
- `lastActivityAt` — latest activity across practice AND mock exams

**Question note:** `correctAnswer` in `POST/PUT /api/admin/questions` must be a single letter A–D, matching the correct option's position. (Previously it was the option's text; see #155.)

The `mockExams` field is a separate object so it can be filtered out per student in a future sharing feature (#125).

## Practice rounds: request and response

Practice rounds are stored per question (#168). The server checks each answer, so
the client never gets the correct answers in advance and never sends its own score.

### `POST /api/practice/attempts`

```json
{ "subjectId": 2 }
```

Starts a round with all questions of the subject. Response (no `correctAnswer` or `explanation`):

```json
{
  "attemptId": 10,
  "questions": [
    { "id": 8, "questionText": "...", "optionA": "...", "optionB": "...", "optionC": "...", "optionD": "...", "imageUrl": null }
  ]
}
```

- `400` if `subjectId` is missing, `404` if the subject doesn't exist, `409` if it has no questions.
- Starting a round closes the student's earlier unfinished practice round (one open practice round per student).

### `POST /api/practice/attempts/{id}/answers`

```json
{ "questionId": 8, "chosenOption": "A" }
```

Response:

```json
{ "correct": false, "correctOption": "B", "explanation": "..." }
```

- `chosenOption` must be `A`, `B`, `C` or `D` (otherwise `400`). `explanation` may be `null`.
- `403` if the round belongs to another user, `409` if the round is finished or the question was already answered, `400` if the question isn't part of the round.

### `POST /api/practice/attempts/{id}/finish`

No request body. Response:

```json
{ "correctAnswers": 5, "totalQuestions": 7 }
```

- `totalQuestions` is all questions in the round; skipped questions count as not correct (nothing is subtracted).
- `409` if the round is already finished, `403` if it belongs to another user.

**Authentication notes:**

- `401 Unauthorized` — the JWT is missing, expired, or invalid. The frontend clears the token and redirects to `/login?expired=1`.
- `403 Forbidden` — the user is logged in but not authorized for this endpoint. The frontend does **not** log them out.
- `401` from `/api/auth/login` means wrong credentials, not session expiry — the frontend does not redirect.

## Roles

Users have a role: `STUDENT` (default for new registrations) or `ADMIN` (#29). Endpoints under `/api/admin/**` require `ADMIN`.
