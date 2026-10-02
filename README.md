# hunter-exam-trainer
A Progressive Web App (PWA) for practicing the Swedish Hunter Exam theory test (Jägarexamen).

---

## Project Overview

Hunter Exam Trainer is a web and mobile-friendly application designed to help users prepare for the Swedish Hunter Theory Examination (Jägarexamen).

The application provides:

- Practice questions by subject
- Mock exam simulation
- Progress and performance statistics
- Recent exam results
- User registration and login
- Password management
- Responsive design for desktop, tablet, and mobile devices
- PWA support

The project is built as a Progressive Web App (PWA), allowing it to run on desktop, tablet, and mobile devices.

---

## Project Objectives

- Build a responsive Progressive Web App
- Provide practice questions for all 15 subjects
- Allow users to practice questions by subject
- Provide a 70-question mock exam simulation
- Track practice and mock exam results
- Display user statistics and progress
- Provide user authentication and authorization
- Prepare the application for future admin functionality

---

## Technology Stack

### Frontend

- React
- TypeScript
- Vite
- React Router
- CSS
- Lucide React
- Progressive Web App (PWA)

### Backend

- Java
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Security
- JWT Authentication
- Bean Validation
- Maven

### Database

- PostgreSQL

### Development Tools

- IntelliJ IDEA
- Git
- GitHub
- Postman

---

## Prerequisites

Before running the project, make sure the following are installed:

- JDK 25 (required for the backend)
- Maven
- Node.js
- npm
- PostgreSQL (locally installed or via Docker)
- Git
- IntelliJ IDEA (recommended)
- Postman (optional, for API testing)

You can verify the installations using:

```bash
java -version
mvn -version
node -v
npm -v
psql --version
git --version
```

Both `java -version` and `mvn -version` must report Java 25. Set `JAVA_HOME`
to your JDK 25 installation and put its `bin` directory on `PATH`, then reopen
your terminal. The Maven build requires JDK 25 or newer, and the Spring Boot parent derives
the compiler release from `java.version` in `backend/pom.xml`.

In IntelliJ, import `backend/pom.xml` as a Maven project and set:

- Project SDK and backend module SDK: JDK 25; language level: SDK default (25).
- Maven importer JDK and Maven runner JRE: JDK 25 (or Project SDK).
- Spring Boot and test run configurations: JDK 25 (or Project SDK).

Reload the Maven project after changing these settings. When switching JDKs,
run `mvn clean test` from `backend` to remove stale compiled classes. If IntelliJ
has built classes into its own output directory, use **Build → Rebuild Project**
as well. The Docker build and runtime also use Java 25. Personal IDE settings
should not be committed.

---

## First-Time Setup

Follow these steps when setting up the project on a new computer:

### 1. Clone the repository

Clone the repository and open the project in IntelliJ IDEA.
```bash
git clone <repository-url> 
cd hunter-exam-trainer
````

### 2. Create the PostgreSQL database

**Option A: Docker (recommended).** This starts PostgreSQL in a container and creates the `hunter_exam` database automatically:

```bash
docker run -d --name hunter-exam-postgres -e POSTGRES_USER=<user> -e POSTGRES_PASSWORD=<password> -e POSTGRES_DB=hunter_exam -p 5432:5432 postgres:16
```

If port 5432 is already in use on your computer, map another host port (e.g. `-p 5433:5432`) and set `DB_URL=jdbc:postgresql://localhost:5433/hunter_exam` (see Environment Variables below).

**Option B: Local PostgreSQL installation.** Create the database used by the backend:

```sql
CREATE DATABASE hunter_exam;
```

The database name must match the configuration in:

```
backend/src/main/resources/application.properties
```

The current configuration uses:

```properties
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/hunter_exam}
```

This means: use the `DB_URL` environment variable if it is set, otherwise connect to local PostgreSQL on port 5432.

### 3. Configure Database Credentials

The application does not store the PostgreSQL username and password directly in `application.properties`.
The following environment variables must be configured:

```
DB_USERNAME
DB_PASSWORD
```

For example, when running the backend from IntelliJ IDEA, these can be added under:

**Run → Edit Configurations → Environment Variables**

The values should match the PostgreSQL account used on the local computer (or the `POSTGRES_USER`/`POSTGRES_PASSWORD` given to the Docker container).

### 4. Configure JWT Secret

The application also requires the following environment variable:

```
JWT_SECRET
```
The application uses a JWT secret for creating and validating authentication tokens.

The secret must be at least 32 characters (256 bits) long, otherwise the backend will fail to start with a `WeakKeyException`.

Each developer should create their **own local JWT secret**. The secret should not be committed to GitHub or shared in the repository.

Add the following environment variable to the backend Run/Debug configuration in IntelliJ IDEA:

```
JWT_SECRET=<your-generated-secret>
```

A Base64-encoded secret can be generated using the terminal:

```
openssl rand -base64 32
```

Copy the generated value and use it as the value of `JWT_SECRET`.

For example:
```
JWT_SECRET=generated-value-goes-here
```

In IntelliJ IDEA:

1. Open Run → Edit Configurations.
2. Select the Spring Boot backend configuration.
3. Find Environment variables.
4. Add JWT_SECRET, ADMIN_EMAIL, and ADMIN_PASSWORD.
5. Paste the generated values.
6. Apply the changes and restart the backend.

The JWT secret is used only by the local backend and should not be added to `application.properties` or committed to GitHub.

**Do not commit the JWT secret to GitHub.**

### Optional: Google Login

Google login is **off by default**, and the backend starts without any Google keys. To turn it on, add these environment variables to the backend Run/Debug configuration:

```
GOOGLE_LOGIN_ENABLED=true
GOOGLE_CLIENT_ID=<client-id-from-google-cloud-console>
GOOGLE_CLIENT_SECRET=<client-secret-from-google-cloud-console>
```

The client ID and secret come from registering the app in Google Cloud Console (#61). When Google login is off, the "Continue with Google" button shows a "not configured yet" message.

**Do not commit the Google client secret to GitHub.**

### Password recovery email (SMTP)

The backend uses Spring Boot Mail and SMTP. Set these variables in the backend's
IntelliJ Run Configuration or hosting environment, never in frontend `VITE_*`
variables or committed files.

| Variable | Purpose | Local default / safe production example |
|----------|---------|-----------------------------------------|
| `MAIL_HOST` | SMTP server | `localhost` / `smtp.example.com` |
| `MAIL_PORT` | SMTP port | `1025` / `587` (STARTTLS) |
| `MAIL_USERNAME` | SMTP login | Empty / `<smtp-username>` |
| `MAIL_PASSWORD` | SMTP password or provider app password | Empty / `<smtp-app-password>` |
| `MAIL_FROM` | Authorized sender address | `no-reply@hunterexam.local` / `no-reply@example.com` |
| `MAIL_SMTP_AUTH` | Enable SMTP authentication | `false` / `true` |
| `MAIL_STARTTLS_ENABLED` | Enable and require STARTTLS | `false` / `true` |
| `FRONTEND_URL` | Trusted frontend base URL for reset links | `http://localhost:5173` / `https://app.example.com` |

For local development, run Mailpit to capture email without external delivery:

```bash
docker run --rm --name hunter-exam-mailpit -p 127.0.0.1:1025:1025 -p 127.0.0.1:8025:8025 axllent/mailpit
```

Start the backend with the local defaults, request a link using Forgot Password,
and open the email at `http://localhost:8025`. Follow the link, enter and confirm
a new password, then log in. Restart the backend after changing its environment.
Production needs an SMTP provider, authorized sender and any required domain
verification, authentication, STARTTLS, and an HTTPS `FRONTEND_URL`. Configure
`APP_CORS_ALLOWED_ORIGINS` and `VITE_API_BASE_URL` as usual. The frontend host must
serve its SPA at `/reset-password`; the existing Vercel rewrite supports this.

Both endpoints are public and accept JSON:

- `POST /api/auth/forgot-password`: `{"email":"student@example.com"}`.
  All valid addresses receive HTTP 200 with
  `{"message":"If an eligible account exists for that email, a password reset link has been sent."}`.
  This includes unknown addresses, Google-only accounts and mail delivery failures.
- `POST /api/auth/reset-password`: `{"token":"<token-from-email>","newPassword":"<new-password>"}`.
  HTTP 200: `{"message":"Password reset successfully. You can now log in."}`.
  Invalid, expired or consumed links return HTTP 400 with
  `{"message":"Reset link is invalid, expired, or already used. Please request a new reset link."}`.
  Input validation also returns HTTP 400 with `{"message":"..."}`. Passwords must
  have at least eight characters, matching registration, and at most 72 UTF-8
  bytes (BCrypt's input limit).

Tokens contain 256 random bits, expire after 30 minutes, and are stored only as
SHA-256 hashes. A new request replaces the previous link. Password update and
token consumption share a locked database transaction. Links use a URL fragment
to keep tokens out of HTTP access logs and referrers; the reset page removes the
fragment from the address bar. Do not enable SMTP message debugging or Hibernate
bind-parameter logging in production. Delivery failures log only a generic warning;
use SMTP provider monitoring to investigate delivery issues.

The existing Hibernate `ddl-auto=update` adds three columns to `users`:
`password_login_enabled` (default true), `password_reset_token_hash` (nullable,
unique, 64 characters), and `password_reset_expires_at` (nullable timestamp).
No new migration framework or token table is needed.

**Classify existing Google-only accounts before exposing password recovery.**
The previous implementation stored random BCrypt passwords for Google users
without recording their account type, so their origin cannot be inferred from
the hash. Existing users default to password-enabled to preserve normal accounts.
Identify known Google-only accounts from your account records and mark them:

```sql
UPDATE users SET password_login_enabled = false,
    password_reset_token_hash = NULL, password_reset_expires_at = NULL
WHERE email IN ('<known-google-only-email>');
```

New Google-only accounts are marked automatically. Google sign-in for an existing
password account preserves recovery eligibility. The shared guest account is
excluded. Existing JWTs retain their current one-hour expiry after a password
reset. SMTP is synchronous, so response times can vary; deployments should
rate-limit recovery requests at their ingress. Response statuses and bodies never
disclose account existence.

Backend tests use H2 and mocked email boundaries; they need no external database
or mail server. With Java 25, run `cd backend` then `./mvnw test` (Windows:
`mvnw.cmd test`). For the frontend run `npm ci` and `npm run build` from `frontend`.
The repository currently has no frontend test runner.

### 5. Start the Backend

Open the `backend` project/module in IntelliJ IDEA and run the Spring Boot application.

The backend runs on:

```
http://localhost:8080
```

On the first successful startup, the application automatically creates/updates the required database tables through JPA.


### 6. Initial Data

The project contains initial subject and question data in:

```
backend/src/main/resources/data/subjects.json
backend/src/main/resources/data/questions.json
```

`DataInitializer` loads this data automatically when the corresponding database table is empty.

- The subjects are loaded from `subjects.json`.
- Questions are loaded from `questions.json` and linked to subjects using the subject name.

For example, if a question contains:

```json
{
  "subject": "Jaktetik"
}
```

the initializer looks for the subject named `Jaktetik` in the database.

If the subject cannot be found, the backend will report:

```
Subject not found: <subject name>
```

> **Important:** The seed data is only inserted when the corresponding table is empty. If the database already contains subjects or questions, starting the application will not reload or replace the existing data.

### 7. Start the Frontend

Navigate to the frontend directory:

```bash
cd frontend
```

Install the dependencies:

```bash
npm install
```

Start the development server:

```bash
npm run dev
```

The frontend normally runs on:

```
http://localhost:5173
```

Open the address in a browser.

## Project Structure

The project is divided into frontend and backend modules.

```
hunter-exam-trainer/
│
├── backend/
│   └── src/
│       └── main/
│           ├── java/
│           │   └── com/hunterexam/backend/
│           │       ├── config/
│           │       ├── controller/
│           │       ├── dto/
│           │       ├── entity/
│           │       ├── repository/
│           │       ├── security/
│           │       └── service/
│           │
│           └── resources/
│               ├── data/
│               │   ├── subjects.json
│               │   └── questions.json
│               └── application.properties
│
├── frontend/
│   └── src/
│       ├── api/
│       ├── components/
│       ├── pages/
│       ├── styles/
│       └── types/
│
├── assets/
├── docs/
├── question-bank/
├── README.md
└── ...
```

## Authentication

The application uses JWT-based authentication.

### Registration

Users can register with:

- Full name
- Email
- Password

Passwords are stored using BCrypt hashing.

### Login

After successful login, the backend returns a JWT token.

The frontend stores the token in local storage and sends it with authenticated API requests using:

​```
Authorization: Bearer <token>
​```

### Protected Routes

Authenticated pages are protected using the frontend `ProtectedRoute`.

Unauthenticated users are redirected to the login page.

### Logout

Logging out removes the JWT token from local storage and redirects the user to the login page.

## Main API Endpoints

### Authentication

| Method | Endpoint             | Description            |
|--------|----------------------|------------------------|
| POST   | `/api/auth/register` | Register a new user    |
| POST   | `/api/auth/login`    | Log in and receive JWT |
| POST   | `/api/auth/forgot-password` | Request a password reset email |
| POST   | `/api/auth/reset-password` | Set a new password using a reset token |
| GET    | `/api/auth/google/status` | Whether Google login is enabled |

### User

| Method | Endpoint                 | Description                                    |
|--------|--------------------------|------------------------------------------------|
| GET    | `/api/users/me`          | Get the currently logged-in user's information |
| PUT    | `/api/users/me/password` | Change the user's password                     |

### Subjects

| Method | Endpoint        | Description                          |
|--------|-----------------|--------------------------------------|
| GET    | `/api/subjects` | Get all subjects and question counts |

### Questions

| Method | Endpoint                        | Description                 |
|--------|---------------------------------|-----------------------------|
| GET    | `/api/questions?subjectId={id}` | Get questions for a subject |

Question management requires a JWT with the `ADMIN` role. Regular users receive
`403 Forbidden`; requests without authentication receive `401 Unauthorized`.

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/admin/questions` | Create a question (201, with Location header) |
| PUT | `/api/admin/questions/{id}` | Replace a question (200) |
| DELETE | `/api/admin/questions/{id}` | Delete a question (204) |

POST and PUT accept the same JSON body:

```json
{
  "questionText": "Which option is correct?",
  "optionA": "First option",
  "optionB": "Second option",
  "optionC": "Third option",
  "optionD": "Fourth option",
  "correctAnswer": "First option",
  "explanation": "Optional explanation",
  "subjectId": 1
}
```

All fields except `explanation` are required. Text fields have a maximum length of
255 characters. `subjectId` must be positive and refer to an existing subject.
`correctAnswer` must exactly match one of the four option texts. Invalid input
returns 400; a missing question or subject returns 404. PUT replaces all editable
fields, including clearing the explanation when omitted.

### Practice

| Method | Endpoint                | Description                       |
|--------|-------------------------|-----------------------------------|
| POST   | `/api/practice/results` | Save a completed practice session |

### Mock Exam

| Method | Endpoint           | Description                   |
|--------|--------------------|-------------------------------|
| POST   | `/api/exam/start`  | Start a 70-question mock exam |
| POST   | `/api/exam/submit` | Submit a completed mock exam  |

### Dashboard

| Method | Endpoint                | Description                  |
|--------|-------------------------|------------------------------|
| GET    | `/api/dashboard`        | Get dashboard statistics     |
| GET    | `/api/dashboard/recent` | Get recent mock exam results |

### Statistics

| Method | Endpoint                   | Description                         |
|--------|----------------------------|-------------------------------------|
| GET    | `/api/statistics/subjects` | Get practice performance by subject |


## Current Features

The current application supports:

- User registration
- User login
- JWT authentication
- Protected routes
- Logout
- User profile information
- Change password
- 15 hunter-exam subjects
- Subject question counts
- Practice questions by subject
- Immediate answer feedback
- Practice result storage
- 70-question mock exam
- 60-minute exam timer
- Automatic exam submission when the timer expires
- Mock exam result calculation
- Mock exam result storage
- Dashboard statistics
- Recent activity
- Practice performance by subject
- Performance summary
- Responsive desktop and mobile layouts
- PWA support

## Hunter Exam Subjects

The application currently contains 15 subjects:

1. Introduktion
2. Jaktetik
3. Ekologi
4. Klövvilt
5. Övriga däggdjur
6. Fåglar: gäss & änder
7. Fåglar: övriga
8. Vapen: hagel
9. Vapen: kula
10. Träffområden
11. Jakthundar
12. Eftersök
13. Viltet efter skottet
14. Lagen
15. Repetition


## Test Data

For local development, a new user can be registered through the application.

The initial subjects and questions are loaded automatically from the JSON seed files when the database tables are empty.

When testing with a fresh database:

1. Create the `hunter_exam` database.
2. Start the backend.
3. `DataInitializer` loads the subjects.
4. `DataInitializer` loads the questions.
5. Start the frontend.
6. Register a user.
7. Log in.
8. Test the Practice and Mock Exam features.


## Development Notes

### Database

The project currently uses:

```
spring.jpa.hibernate.ddl-auto=update
```

This allows the Hibernate/JPA to update the database schema based on the entity definitions during development.

This configuration is intended for the current development setup and should be reviewed before production deployment.

SQL logging is currently enabled:

```
spring.jpa.show-sql=true
```

This is useful during development and debugging.

## Temporary Deployment (PWA Testing)

The app is temporarily deployed to free hosting so we can test the **PWA install workflow on a real mobile device** — mobile browsers require HTTPS to install a PWA, which `localhost` cannot provide.

This deployment is **not production, and not a permanent staging environment.** It exists to validate the PWA install flow and to let the team demo the app on real phones. If it stops being useful, it can be torn down without affecting local development.

### URLs

| Service | URL | Platform |
|---|---|---|
| Backend | https://hunter-exam-trainer-bak.onrender.com | Render (Docker) |
| Frontend | https://hunter01-kappa.vercel.app | Vercel (Vite) |
| Database | Neon (same instance as local dev) | — |

### How It Works

- Both services are connected to the repo and pick up code changes from `develop`.
- Configuration is via environment variables on each platform. No secrets are committed to the repo.
- The deployed backend uses the same Neon database as local development — test data created on the deployment will also appear locally.

### Environment Variables

**On Render (backend):**

| Variable | Value / Notes |
|---|---|
| `DB_URL` | Neon connection string |
| `DB_USERNAME` | Neon username |
| `DB_PASSWORD` | Neon password |
| `JWT_SECRET` | Same Base64 secret used locally |
| `ADMIN_EMAIL` | Admin seed email |
| `ADMIN_PASSWORD` | Admin seed password |
| `APP_CORS_ALLOWED_ORIGINS` | `http://localhost:5173,https://hunter01-kappa.vercel.app` |

**On Vercel (frontend):**

| Variable | Value |
|---|---|
| `VITE_API_BASE_URL` | `https://hunter-exam-trainer-bak.onrender.com` |

⚠️ **Vite env vars are baked in at build time.** Changing `VITE_API_BASE_URL` on Vercel has no effect until the frontend is redeployed with the cache disabled.

⚠️ **Set Vercel env vars for all environments** (Production, Preview, Development) — otherwise preview deployments fall back to `localhost:8080`.

### Known Limitations

- **Render free tier sleeps after ~15 minutes of inactivity.** The first request after sleep takes 30–60 seconds.
- **No test gate before deploy.** Pushing to `develop` deploys whatever is on `develop` — a broken `develop` will produce a broken deployed app.
- **CORS origins must match exactly** — including `https://` vs `http://` and no trailing slash.

### Testing on Mobile (PWA Install)

- **iOS Safari:** open the frontend URL → tap Share → **Add to Home Screen**
- **Android Chrome:** open the frontend URL → tap ⋮ menu → **Install app**

Once installed, the app launches full-screen and behaves like a native app.

### Current Deployment Setup Is Temporary

The Vercel project is deployed from a **standalone copy of the frontend repo** because the main `hunter-exam-trainer` repo is org-owned and requires org-level approval for the Vercel GitHub App. Once that approval is granted, the plan is to:

1. Create a new Vercel project on the main repo, watching `develop`
2. Update `APP_CORS_ALLOWED_ORIGINS` on Render with the new URL
3. Retire the standalone frontend repo

Until then, treat this deployment as a **test rig for PWA workflows**, not as our canonical staging environment.

### Frontend API Configuration

The frontend reads the API base URL from `VITE_API_BASE_URL`:

- **Local dev:** defaults to `http://localhost:8080` when the env var is not set.
- **Deployed:** set on Vercel to the temporary deployment backend URL (see "Temporary Deployment (PWA Testing)").

The frontend development server runs on:

```
http://localhost:5173
```


The backend CORS configuration allows requests from the frontend development server and from the deployed Vercel URL (configured via `APP_CORS_ALLOWED_ORIGINS`).

## Current Project Status

The main student-facing functionality has been implemented, including authentication, practice questions, mock exams, result storage, dashboard statistics, and subject performance statistics.

The application is currently suitable for further development and testing.

## Future Work

Possible future improvements include:

- Admin authentication/authorization
- Admin interface for question management
- More detailed statistics
- Question review/history
- Improved exam result history
- Practice mode - Random Practice
- Offline support for exam content (currently only the app shell is cached for install/fast load, not questions or results)
- Production deployment
- Automated backend and frontend tests
- Improved exceptional/error handling
- Production database configuration


## Git Workflow

Before starting work:

```
git pull
```

Create or switch to your development branch before making changes.

After completing a task:

```
git status
git add .
git commit -m "describe your change"
git push
```

Keep commits focused on a specific change where possible.

When working with other team members, pull the latest changes before starting new work to reduce merge conflicts.

## Troubleshooting

### Backend does not start

Check:

- Java version
- Maven installation
- PostgreSQL is running
- PostgreSQL database `hunter_exam` exists
- `DB_USERNAME` is configured
- `DB_PASSWORD` is configured
- `JWT_SECRET` is configured

### Database connection error

Check the configuration:

```
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/hunter_exam}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

Make sure the PostgreSQL server is running, the port matches (`DB_URL` if not 5432) and the credentials are correct.

### Questions or subjects are not loaded

Check that these files exist:

```
backend/src/main/resources/data/subjects.json
backend/src/main/resources/data/questions.json
```

Remember that `DataInitializer` only loads data when the corresponding table is empty.

Also make sure that every subject referenced by `questions.json` exists in `subjects.json`.

### Frontend cannot connect to backend

Make sure both applications are running:

```
Frontend: http://localhost:5173
Backend:  http://localhost:8080
```

Also check the browser console and backend logs for errors.


## Handover Notes

Before making changes to the project:

1. Pull the latest changes from GitHub.
2. Make sure the backend starts successfully.
3. Make sure the frontend starts successfully.
4. Verify that PostgreSQL is running.
5. Check that the required environment variables are configured.
6. Test login before testing protected functionality.
7. If using a new database, allow `DataInitializer` to load the initial subjects and questions.
8. Keep the JSON question data and subject names consistent.
9. Test the affected feature before committing changes.
10. Commit and push changes to your development branch.

For questions or changes to the database structure, review the entity, repository, service, controller, and DTO classes together because changes in one layer may require corresponding changes in the other layers.
