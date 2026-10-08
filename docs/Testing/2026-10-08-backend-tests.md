# Test: Backend unit and web-layer tests for the original features

- **Date:** 2026-10-06 to 2026-10-08
- **Branch / commit:** `feature/37-backend-tests`, full run at commit `735ac39` (after merging `develop`)
- **Related issues:** #37 (also covers the session and time-limit rules from #35)

## What we tested and why

The project plan asks for a working, tested application. The backend only had the default test that checks the application starts. The features built by the previous team (exam scoring, register/login, JWT, statistics) had no tests, so a change could break them without anyone noticing. This round adds tests for those features, starting where a bug would hurt users most.

| Test file (under `backend/src/test/java/com/hunterexam/backend/`) | What it covers | Tests |
|---|---|---|
| `service/ExamServiceTests.java` | scoring (all correct, mixed, invalid letter, rounding, saved result and user); session rules (no session id, unknown session, already submitted, over 60 minutes, just under 60 minutes); starting an exam (fewer than 70 questions, 70 different questions and the session id) | 12 |
| `service/AuthServiceTests.java` | register (hashed password and STUDENT role, duplicate email); login (right password gives a token, wrong password, unknown email); Google login (moved here from `PasswordResetServiceTests`) | 6 |
| `security/JwtServiceTests.java` | token contains email, role and a one-hour lifetime; a token can't be verified with another secret | 2 |
| `controller/ExamControllerTests.java` | logged-out user gets 401 on `POST /api/exam/start`; logged-in student gets through | 2 |
| `service/StatisticsServiceTests.java` | average score per subject; a new user gets an empty list | 2 |

A student getting 403 on `/api/admin/**` was already covered by `AdminQuestionControllerTests` (one central rule in `SecurityConfig`), so it was not duplicated.

Also in this round:

- Shared test settings moved to `backend/src/test/resources/application-test.properties`, activated with `@ActiveProfiles("test")` (H2 database, test JWT secret, test admin account).
- The stale guest-account case removed from `PasswordResetServiceTests` and the guest check from `PasswordResetService` (guest login was removed in #77).
- `JwtService` tidied: token lifetime as a named constant. `JwtServiceTests` passed before and after (no behaviour change).

## How

1. **Tools:** JUnit 5 and Mockito (already used in the project).
   - **Service tests** (`@ExtendWith(MockitoExtension.class)`): the service is real; repositories and other services are `@Mock` fakes, scripted with `when(...).thenReturn(...)`. No Spring and no database, so they run in milliseconds. Password tests use the real `BCryptPasswordEncoder`.
   - **Web-layer tests** (`@WebMvcTest` + `@Import(SecurityConfig.class)`): the real controller and security rules; the service is a `@MockitoBean`. Requests are sent with `MockMvc`; `jwt()` fakes a logged-in user.
2. **Layout:** test classes mirror the production classes (`ExamService` → `ExamServiceTests`, same package). Each test has a descriptive name and Arrange / Act / Assert sections.
3. **Run:** in `backend/`, `./mvnw test` (all tests) or `./mvnw test -Dtest=ExamServiceTests` (one class). In IntelliJ, the green ▶ next to a test class or method.
4. **Checking that the tests can fail:** for every new test, the production code was temporarily broken in a way matching that test, the tests were run, and the change was undone. Examples:
   - storing the password as plain text; removing the duplicate-email check; inverting the password check;
   - counting correct answers as incorrect; truncating instead of rounding the score; removing the time-limit check; lowering the limit to 50 minutes;
   - not signing the JWT; changing the token lifetime;
   - opening `/api/exam/**` with `permitAll()`; denying every request;
   - averaging the wrong field; returning `null` for a user with no results.

   One test (submit without a session id) still passed with the code broken: the error happened one step later with the same exception type. It was tightened with `verifyNoInteractions(...)` and checked again. Tests with a broad exception type (`RuntimeException`) got such an extra check from the start (a `verifyNoInteractions(...)` or the exception message).

## Result

- Normal run (`./mvnw test`, whole backend): **Tests run: 52, Failures: 0, Errors: 0, Skipped: 0**, BUILD SUCCESS. 23 of these are new in this round (plus one moved).
- With the code deliberately broken: every new test failed for the problem it is meant to catch.

## Conclusion

The original backend features now have automated tests for scoring, the exam session rules, login, tokens, the basic access rule and the statistics numbers. Writing them also uncovered problems in the production code, recorded for follow-up issues (not fixed in #37):

- Students can create, edit and delete subjects: `@PreAuthorize` on `SubjectController` is ignored because method security is not enabled.
- Exam submit trusts the client's list of questions and doesn't check who owns the session; exam sessions are probably saved without a user.
- A token can expire in the middle of a mock exam.
- `GlobalExceptionHandler` chooses the HTTP status by searching the error message text.
- Too few questions gives an unclear message instead of switching the mock exam off.

Follow-up ideas for testing:

- Run the backend and frontend tests automatically on every pull request (GitHub Actions), so each PR shows that the tests passed on that exact commit.
- Tests for newer features without backend tests yet: statistics over time and the admin learner view.
- Configure Mockito as a Java agent in `pom.xml` (the JDK warns that dynamic agent loading will be disallowed in a future version).
