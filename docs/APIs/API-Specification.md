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
| POST   | /api/practice/results         | Save a completed practice session result        | Required         |
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

The `mockExams` field is a separate object so it can be filtered out per student in a future sharing feature (#125).

**Authentication notes:**

- `401 Unauthorized` — the JWT is missing, expired, or invalid. The frontend clears the token and redirects to `/login?expired=1`.
- `403 Forbidden` — the user is logged in but not authorized for this endpoint. The frontend does **not** log them out.
- `401` from `/api/auth/login` means wrong credentials, not session expiry — the frontend does not redirect.

## Roles

Users have a role: `STUDENT` (default for new registrations) or `ADMIN` (#29). Endpoints under `/api/admin/**` require `ADMIN`.
