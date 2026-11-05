### Manual

1. Deleted stale pre-fix sessions:
   `DELETE FROM exam_sessions WHERE user_id IS NULL;`
2. Started backend and frontend locally, logged in.
3. Started a mock exam.
4. Verified in the database (session id 42, user id 8):
    - `SELECT id, user_id FROM exam_sessions ...` → `user_id = 8` 
    - `SELECT * FROM exam_session_questions WHERE session_id = 42` → 70 rows 
5. Answered all 70 questions and submitted. Result page displayed:
    - Score 74%
    - 52 correct / 18 incorrect / 0 unanswered