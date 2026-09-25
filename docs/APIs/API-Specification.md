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
| GET    | /api/dashbaord                | Get dashboard statistics for the logged-in user | Required       |
| GET    | /api/dashboard/recent         | Get the user's recent mock exam activity        | Required       |
| GET    | /api/statistics/subjects      | Get the user's practice performance by subject  | Required       |
