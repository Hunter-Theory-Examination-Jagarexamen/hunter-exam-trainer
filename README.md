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

- Java 25 or compatible Java version used by the project
- Maven
- Node.js
- npm
- PostgreSQL (or a hosted PostgreSQL service such as Neon)
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

---

## First-Time Setup

Follow these steps when setting up the project on a new computer:

### Optional: Using Neon (hosted PostgreSQL)

Instead of running PostgreSQL locally, the project can use a hosted instance on [Neon](https://neon.tech).

1. Create a Neon project and copy the connection string from the dashboard.
2. Set `spring.datasource.url` in `application.properties` to the Neon URL, for example:
3. Set the `DB_USERNAME` and `DB_PASSWORD` environment variables to your Neon credentials.

Note: the Neon **pooler** endpoint works for the running application, but if Hibernate's first-run schema creation fails with a DDL error, temporarily switch to the direct (non-pooler) endpoint.

### 1. Clone the repository

Clone the repository and open the project in IntelliJ IDEA.
```bash
git clone <repository-url> 
cd hunter-exam-trainer
````

### 2. Create the PostgreSQL database

Create the database used by the backend:

```sql
CREATE DATABASE hunter_exam;
```

The database name must match the configuration in:

```
backend/src/main/resources/application.properties
```

The current configuration uses:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/hunter_exam

```
or a hosted PostgreSQL service such as Neon
```
spring.datasource.url=jdbc:postgresql://ep-empty-glade-b4cs37rk-pooler.c-6.us-east-2.aws.neon.tech/neondb?sslmode=require&channel_binding=require
```

### 3. Configure Database Credentials

The application does not store the PostgreSQL username and password directly in `application.properties`.
The following environment variables must be configured:

```
DB_USERNAME
DB_PASSWORD
```

For example, when running the backend from IntelliJ IDEA, these can be added under:

**Run → Edit Configurations → Environment Variables**

The values should match the PostgreSQL account used on the local computer (or your Neon database user).

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
4. Add JWT_SECRET.
5. Paste the generated value.
6. Apply the changes and restart the backend.

The JWT secret is used only by the local backend and should not be added to `application.properties` or committed to GitHub.

**Do not commit the JWT secret to GitHub.**

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

### Environment Variables

The backend requires:

```
DB_USERNAME
DB_PASSWORD
JWT_SECRET
```

These values should be configured locally and should not be committed to the repository.

### Frontend API Configuration

The frontend API client currently communicates with:

```
http://localhost:8080
```

The frontend development server runs on:

```
http://localhost:5173
```

The backend CORS configuration allows requests from the frontend development server.

## Current Project Status

The main student-facing functionality has been implemented, including authentication, practice questions, mock exams, result storage, dashboard statistics, and subject performance statistics.

The application is currently suitable for further development and testing.

## Future Work

Possible future improvements include:

- Admin authentication/authorization
- Admin interface for question management
- Add, edit and delete questions
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
- PostgreSQL is running (or Neon is reachable)
- PostgreSQL database `hunter_exam` exists
- `DB_USERNAME` is configured
- `DB_PASSWORD` is configured
- `JWT_SECRET` is configured

### Database connection error

Check the configuration:

```
spring.datasource.url=jdbc:postgresql://localhost:5432/hunter_exam
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

Make sure the PostgreSQL server is running (or Neon is reachable) and the credentials are correct.

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
4. Verify that PostgreSQL is running (or that Neon is reachable).
5. Check that the required environment variables are configured.
6. Test login before testing protected functionality.
7. If using a new database, allow `DataInitializer` to load the initial subjects and questions.
8. Keep the JSON question data and subject names consistent.
9. Test the affected feature before committing changes.
10. Commit and push changes to your development branch.

For questions or changes to the database structure, review the entity, repository, service, controller, and DTO classes together because changes in one layer may require corresponding changes in the other layers.