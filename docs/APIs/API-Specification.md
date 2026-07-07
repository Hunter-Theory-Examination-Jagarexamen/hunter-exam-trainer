## API Specification

| Method | Endpoint                   | Purpose                      |
|--------|----------------------------|------------------------------|
| POST   | /api/auth/register         | Register new user            |
| POST   | /api/auth/login            | User login                   |
| GET    | /api/users/me              | Get logged-in user's profile |
| GET    | /api/subjects              | List all subject areas       |
| GET    | /api/questions             | List all questions           |
| GET    | /api/questions/{id}        | Get a specific question      |
| GET    | /api/questions/{subjectId} | Get questions by subject     |
| POST   | /api/practice/start        | Start a practice session     |
| POST   | /api/exam/start            | Start a mock exam            |
| POST   | /api/exam/submit           | Submit exam answers          |
| GET    | /api/results               | View previous results        |
| GET    | /api/statistics            | View learning statistics     |
| GET    | /api/admin/questions       | List all questions (Admin)   |
| POST   | /api/admin/questions       | Add a new question           |
| PUT    | /api/admin/questions/{id}  | Update a question            |
| DELETE | /api/admin/questions/{id}  | Delete a question            |
