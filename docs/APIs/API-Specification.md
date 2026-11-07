## API Specification

This file is the canonical list of endpoints. README.md only links here; new
endpoints are added to this table, not duplicated elsewhere.

| Method | Endpoint                      | Purpose                                         | Authentication   |
|--------|-------------------------------|-------------------------------------------------|------------------|
| POST   | /api/auth/register            | Register new user                               | Public           |
| POST   | /api/auth/login               | Authenticate user and return JWT                | Public           |
| POST   | /api/auth/forgot-password     | Request a password reset email                  | Public           |
| POST   | /api/auth/reset-password      | Set a new password using a reset token          | Public           |
| POST   | /api/auth/oauth2/exchange     | Exchange a one-time OAuth2 code for an app JWT  | Public           |
| GET    | /api/auth/google/status       | Whether Google login is enabled                 | Public           |
| GET    | /api/users/me                 | Get logged-in user's profile                    | Required         |
| PUT    | /api/users/me/password        | Change the logged-in user's password            | Required         |
| GET    | /api/subjects                 | Get all subjects and question counts            | Required         |
| GET    | /api/subjects/{id}            | Get one subject                                 | Required         |
| POST   | /api/admin/subjects           | Create a new subject                            | Required (ADMIN) |
| PUT    | /api/admin/subjects/{id}      | Update an existing subject                      | Required (ADMIN) |
| DELETE | /api/admin/subjects/{id}      | Delete a subject                                | Required (ADMIN) |
| POST   | /api/practice/attempts        | Start a practice round for a subject            | Required         |
| POST   | /api/practice/attempts/random | Start a random practice round (questions from all subjects) | Required |
| POST   | /api/practice/attempts/{id}/answers | Answer one question in a practice round (checked by the server) | Required |
| POST   | /api/practice/attempts/{id}/finish | Finish a practice round and get its totals | Required         |
| POST   | /api/exam/start               | Start a mock exam and receive 70 questions      | Required         |
| POST   | /api/exam/{attemptId}/answer | Save or change one answer in the running mock exam | Required |
| POST   | /api/exam/submit              | Submit the mock exam; the server scores the saved answers | Required |
| POST   | /api/exam/{attemptId}/abandon | End the running mock exam without scoring it | Required         |
| GET    | /api/dashboard                | Get dashboard statistics for the logged-in user | Required         |
| GET    | /api/dashboard/recent         | Get the user's 5 most recent scored mock exams  | Required         |
| GET    | /api/statistics/subjects      | Get the user's performance by subject (practice and mock exams) | Required |
| GET    | /api/statistics/progress      | Get the user's weekly accuracy over time        | Required         |
| GET    | /api/admin/learners           | Get aggregated progress for all learners        | Required (ADMIN) |
| GET    | /api/admin/stats              | Get question, subject and learner counts, and unhandled feedback | Required (ADMIN) |
| GET    | /api/admin/questions?subjectId={id} | Get a subject's questions, including the correct answer (admin Question Bank) | Required (ADMIN) |
| GET    | /api/admin/questions/{id}     | Get one question, including the correct answer (`404` if unknown) | Required (ADMIN) |
| POST   | /api/admin/questions          | Create a question (201, with `Location` header) | Required (ADMIN) |
| PUT    | /api/admin/questions/{id}     | Replace a question (200)                        | Required (ADMIN) |
| DELETE | /api/admin/questions/{id}     | Delete a question (204)                         | Required (ADMIN) |
| POST   | /api/feedback                 | Send feedback or report a question (201)        | Required         |
| GET    | /api/admin/feedback?type=&handled= | List feedback, newest first (filters optional) | Required (ADMIN) |
| PATCH  | /api/admin/feedback/{id}/handled | Mark feedback as handled or unhandled         | Required (ADMIN) |

Subject writes were moved from `/api/subjects` to `/api/admin/subjects`. The old
paths have no write handlers anymore: an ADMIN call to them answers
`405 Method Not Allowed`, and STUDENT or anonymous calls are rejected with
`403`/`401` by the security rules before dispatch. Reads stay on
`GET /api/subjects` and `GET /api/subjects/{id}` for any authenticated user.

## Question management: request and response

`POST /api/admin/questions` and `PUT /api/admin/questions/{id}` accept the same
JSON body:

```json
{
  "questionText": "Which option is correct?",
  "optionA": "First option",
  "optionB": "Second option",
  "optionC": "Third option",
  "optionD": "Fourth option",
  "correctAnswer": "A",
  "explanation": "Optional explanation",
  "imageUrl": "https://example.com/images/questions.jpg",
  "subjectId": 1
}
```

All fields except `explanation` and `imageUrl` are required. Text fields have a
maximum length of 255 characters. `imageUrl` is optional and supports an image
illustration URL. `subjectId` must be positive and refer to an existing subject.
`correctAnswer` must be a single letter `A`-`D` matching the correct option's
position (see the question note below). Invalid input returns 400; a missing
question or subject returns 404. PUT replaces all editable fields, including
clearing the explanation or image URL when omitted. Students receive
`403 Forbidden` on these admin routes, and requests without a token
`401 Unauthorized`.

## Subject management: request and response

`POST /api/admin/subjects` and `PUT /api/admin/subjects/{id}` accept:

```json
{ "name": "Wildlife", "description": "Animals" }
```

Both fields are required and limited to 255 characters. `POST` answers `201`,
`PUT` answers `200`, `DELETE /api/admin/subjects/{id}` answers `204`. A duplicate
name or a subject still in use returns `409`, an unknown id `404`.

## Mock exam: request and response

A mock exam is stored as an attempt of type `MOCK_EXAM`, in the same tables as
practice rounds (`attempts` + `attempt_questions`); `attemptId` is the
exam's id in those tables. The server enforces the time limit from the exam's start.

### `POST /api/exam/start`

No request body. Creates a mock exam for the logged-in user with 70 random
questions, stored in the order shown, and returns them without the correct
answers. `409 Conflict` if there are fewer than 70 questions.

Any mock exam of the same student that is still running (tab closed, abandoned
through the menu, ...) is closed **without scoring it** first, so at most one
mock exam can be open at a time. Practice rounds are never touched by this.

```json
{
  "attemptId": 12,
  "questions": [
    { "id": 5, "questionText": "...", "optionA": "...", "optionB": "...", "optionC": "...", "optionD": "...", "imageUrl": null }
  ]
}
```

### `POST /api/exam/{attemptId}/answer`

```json
{ "questionId": 5, "answer": "A" }
```

- Saves one answer as the student gives it (the page calls it on each
  selection), so a lost connection, expired token or closed tab doesn't lose it.
- Answering the same question again **replaces** the earlier answer, until the
  exam is submitted. (Practice is different: each answer is final.)
- `answer` must be `A`, `B`, `C` or `D` (`400` otherwise).
- Response: `204 No Content`. Nothing is said about correct or not before submit.
- `404` if the exam doesn't exist (a practice round's id also gives `404`),
  `403` if it belongs to another user, `409` if it is already submitted,
  `400` if the question isn't part of the exam.

### `POST /api/exam/submit`

```json
{ "attemptId": 12 }
```

- `attemptId` is required and must come from `/api/exam/start`.
- The server scores the answers saved with `/answer`; unanswered questions count
  as not correct. The client sends only the exam's id, no answers or question list.
- Submitting closes the exam (no more answers or submits) and adds each
  answered question to the student's per-subject totals.
- `400` if more than 60 minutes (+ 2 minutes grace) have passed since start: the
  exam is closed, but not scored. `409` if already submitted, `403` if it
  belongs to another user, `404` if it doesn't exist.

Response:

```json
{ "totalQuestions": 70, "correctAnswers": 52, "incorrectAnswers": 10, "unanswered": 8, "score": 74 }
```

### `POST /api/exam/{attemptId}/abandon`

No request body. Ends the student's running mock exam **without scoring it**:
the attempt is closed (finish time set, `scored` stays `false`), so it can never
be answered or submitted again and never appears as a result, in statistics, or
in the subject totals. Used when the student confirms leaving a running exam
through the menu or the browser's back button.

- Response: `204 No Content`.
- `409` if the exam is already submitted or abandoned (treat as "already
  ended"), `403` if it belongs to another user, `404` if it doesn't exist —
  a practice round's id also gives `404`.

**MockExams notes:**

- `GET /api/admin/learners` returns one entry per learner with:
- `subjectPerformances` — performance per subject, weakest first (same numbers as `/api/statistics/subjects`)
- `mockExams` — mock exam summary (count, latest/best score, latest pass/fail), scored mock exams only
- `lastActivityAt` — the latest answer (practice or mock exam) or mock exam submit; `null` if none

**Question note:** `correctAnswer` in `POST/PUT /api/admin/questions` must be a single letter A–D, matching the correct option's position. (Previously it was the option's text; see #155.)

The `mockExams` field is a separate object so it can be filtered out per student in a future sharing feature (#125).

## Statistics: what the numbers count

The statistics are calculated from the stored answers (`attempts`,
`attempt_questions`) and the per-subject totals (`subject_stats`).

- **Counted answers:** every answered practice question, and the answers of
  **scored** mock exams. A mock exam is scored when it was submitted in time;
  open exams and late submits (`400`) don't count anywhere. Unanswered
  questions are not counted as answers (in a mock exam's score they count as
  not correct).
- **Per subject** (`/api/statistics/subjects`, `subjectPerformances` in
  `/api/admin/learners`): `percentage` = correct ÷ answered over all counted
  answers in the subject, rounded. Only subjects with at least one answer are
  listed. Student and admin see the same numbers.
- **Progress over time** (`/api/statistics/progress?weeks=N`): one point per
  week (Monday) with counted answers, oldest first; `accuracy` = correct ÷
  answered that week, `questionsAnswered` = answers that week.
- **Dashboard** (`/api/dashboard`): `mockExams`, `averageScore` and
  `bestScore` from scored mock exams; `questionsAnswered` = all counted answers
  (practice and mock exams).
- **Recent activity** (`/api/dashboard/recent`): the 5 most recent scored mock
  exams, newest first. `id` is the exam's attempt id; `score` is correct ÷
  total questions, rounded.

## Practice rounds: request and response

Practice rounds are stored per question. The server checks each answer, so
the client never gets the correct answers in advance and never sends its own score.

### `POST /api/practice/attempts`

```json
{ "subjectId": 2 }
```

Starts a round with all questions of the subject. Response (no `correctAnswer` or `explanation`):

```json
{
  "attemptId": 10,
  "subjectName": "Viltvård",
  "questions": [
    { "id": 8, "questionText": "...", "optionA": "...", "optionB": "...", "optionC": "...", "optionD": "...", "imageUrl": null }
  ]
}
```

- `400` if `subjectId` is missing, `404` if the subject doesn't exist, `409` if it has no questions.
- Starting a round (either start endpoint) closes the student's earlier unfinished practice round (one open practice round per student).

### `POST /api/practice/attempts/random`

No request body. Starts a random practice round: the server picks up to
**20 questions** at random from the whole question bank, across all subjects.
The questions are spread over as many subjects as the round size allows (a
subject with many questions can't fill the round alone), each question appears
at most once, and the order is random. A smaller bank gives a shorter round.

Response has the same shape as `/api/practice/attempts`, but `subjectName` is
`null`, because a random round mixes subjects:

```json
{
  "attemptId": 11,
  "subjectName": null,
  "questions": [
    { "id": 8, "questionText": "...", "optionA": "...", "optionB": "...", "optionC": "...", "optionD": "...", "imageUrl": null }
  ]
}
```

- `409 Conflict` if the question bank is empty.
- The round is answered and finished exactly like a subject round: `/answers`
  saves and checks each answer (attributed to that question's subject in the
  statistics), `/finish` returns the totals.

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
{ "correctAnswers": 5, "answeredQuestions": 6, "totalQuestions": 7 }
```

- `totalQuestions` is all questions in the round, `answeredQuestions` how many were answered (skipped questions are not counted, nothing is subtracted).
- A round that was answered at least once is also saved for the statistics, counting only the answered questions (e.g. 5 of 6, not 5 of 7).
- `409` if the round is already finished, `403` if it belongs to another user.

## Feedback: request and response

`POST /api/feedback` (any logged-in user) accepts:

```json
{ "type": "QUESTION", "text": "Fel svar markerat som rätt", "questionId": 12 }
```

- `type` is `QUESTION`, `APP` or `OTHER` (required). `text` is required, at
  most 2000 characters; spaces around it are removed.
- `questionId` is optional and only allowed with `type` `QUESTION`; another
  type gives `400`, an unknown question `404`.
- Answers `201` with no body. One user can send at most 20 items in 24 hours;
  after that the answer is `429`.

`GET /api/admin/feedback` returns items newest first. Optional filters:
`type` (`QUESTION`, `APP`, `OTHER`) and `handled` (`true`/`false`); an unknown
value gives `400`.

```json
[{ "id": 3, "type": "QUESTION", "text": "Fel svar markerat som rätt",
   "createdAt": "2026-11-06T10:15:00", "handled": false,
   "questionId": 12, "questionText": "Vilket djur ..." }]
```

- The sender is stored but never returned, so admins can't see who sent it.
- `questionText` is the question's text when it was reported. `questionId` is
  `null` if the question has been deleted since.

`PATCH /api/admin/feedback/{id}/handled` accepts `{ "handled": true }` (or
`false` to undo) and returns the updated item; unknown id `404`.

`GET /api/admin/stats` also returns `unhandledFeedbackCount`.

**Authentication notes:**

- `401 Unauthorized` — the JWT is missing, expired, or invalid. The frontend clears the token and redirects to `/login?expired=1`.
- `403 Forbidden` — the user is logged in but not authorized for this endpoint. The frontend does **not** log them out.
- `401` from `/api/auth/login` means wrong credentials, not session expiry — the frontend does not redirect.

## Roles

Users have a role: `STUDENT` (default for new registrations) or `ADMIN`. Endpoints under `/api/admin/**` require `ADMIN`.
