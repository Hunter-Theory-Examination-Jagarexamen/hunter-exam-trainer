## API Specification

| Method | Endpoint                      | Purpose                                         | Authentication |
|--------|-------------------------------|-------------------------------------------------|----------------|
| POST   | /api/auth/register            | Register new user                               | Public         |
| POST   | /api/auth/login               | Authenticate user and return JWT                | Public         |
| GET    | /api/users/me                 | Get logged-in user's profile                    | Required       |
| PUT    | /api/users/me/password        | Change the logged-in user's password            | Required       |
| GET    | /api/subjects                 | Get all subjects and question counts            | Required       |
| GET    | /api/questions?subjectId={id} | Get questions for a specific subject            | Required       |
| POST   | /api/practice/results         | Save a completed practice session result        | Required       |
| POST   | /api/exam/start               | Start a mock exam and receive 70 questions      | Required       |
| POST   | /api/exam/submit              | Submit a mock exam answers and save the result  | Required       |
| GET    | /api/dashboard                | Get dashboard statistics for the logged-in user | Required       |
| GET    | /api/dashboard/recent         | Get the user's recent mock exam activity        | Required       |
| GET    | /api/statistics/subjects      | Get the user's practice performance by subject  | Required       |

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
  "questionIds": [5, 17, 42],
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

## Roles

Users have a role: `STUDENT` (default for new registrations) or `ADMIN` (#29). Endpoints under `/api/admin/**` require `ADMIN`. No admin endpoints exist yet; the path is reserved for future admin features.
