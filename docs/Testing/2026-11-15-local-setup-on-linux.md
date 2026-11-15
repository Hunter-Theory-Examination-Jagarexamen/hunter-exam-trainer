# Test: Local setup on Linux, following the README

- **Date:** 2026-11-15
- **Branch / commit:** `develop` at `78d5f2e` (Merge PR #233)
- **Related issues:** none yet (findings below are candidates for README updates)

## What we tested and why

Whether a new team member on Linux can set up and run the whole app by
following the README as written. The first local setup test
(2026-09-18-local-setup-verified.md) was done on Windows; this is the Linux
counterpart, on a fresh Linux Mint 22.3 install (based on Ubuntu 24.04).

## How

1. **Docker:** Docker Engine from Docker's official apt repository (Ubuntu
   "noble" packages), user added to the `docker` group.
2. **Database:** PostgreSQL 16 in Docker, with the README's step 2 command
   (`docker run ... postgres:16`).
3. **JDK:** Temurin 25 through SDKMAN, which also sets `JAVA_HOME`.
   `./mvnw -version` reported Maven 3.9.16 on Java 25.0.5; no separate Maven
   install.
4. **Node:** Node 26 (current LTS) and npm 11 through nvm.
5. **IntelliJ IDEA 2026.3**, opened on the repo root, `backend/pom.xml` loaded
   as a Maven project, Project SDK set to the SDKMAN JDK. Backend started from
   an **Application** run configuration (main class
   `com.hunterexam.backend.BackendApplication`) with the environment variables
   from the README table. Annotation processing enabled when IntelliJ asked
   (Lombok).
6. **Frontend:** in `frontend/`, `npm install`, `npm run dev`, then logged in
   at `http://localhost:5173`.
7. **Checks:** `npm run build`, `npx vitest run`, `npm run lint`,
   `npm audit --omit=dev`.

## Result

The full stack ran locally on Linux: the backend started against PostgreSQL
(`Started BackendApplication in 6.4 seconds`), loaded the question bank, and
the frontend logged in and showed the questions.

| Check | Result |
|---|---|
| `npm run build` | OK |
| `npx vitest run` | 44 test files, 297 tests passed |
| `npm run lint` | 6 errors, 3 warnings, all in existing code (not caused by the setup) |
| `npm audit --omit=dev` | 0 vulnerabilities (the 21 reported by `npm install` are all in development tools) |

Problems met on the way, and gaps in the README:

- **Docker is not in the Prerequisites list**, although step 2 recommends it.
- **Maven is listed but not needed**: the repo ships the Maven wrapper.
  `./mvnw -version` is the check to use instead of `mvn -version`.
- **`psql --version` fails with the Docker option**, since `psql` is then not
  installed on the computer.
- **No Linux instructions for `JAVA_HOME`.** SDKMAN sets it.
- **No Node version is given.** The project's dependencies need Node 22.12 or
  newer (the strictest is Vitest 5). Suggested: an `engines` field in
  `frontend/package.json` and the version in the Prerequisites.
- **The Docker container does not start after a reboot**: the `docker run`
  command has no restart policy. `--restart unless-stopped` fixes it.
- **Copying the `<user>` / `<password>` placeholders as-is fails in bash**,
  because `<` means "read from a file".
- **"Spring Boot run configuration" is a paid IntelliJ feature.** The free
  IntelliJ uses an Application run configuration, with the same Environment
  variables field.
- **Environment variables are only explained for IntelliJ**, not for VS Code
  or the terminal.
- **Copying environment variables between IntelliJ installations added a
  backslash before `=` in `JWT_SECRET`.** Startup failed with
  `Illegal base64 character: '\'` in `SecurityConfig.jwtSecretKey`. Removing
  the backslash (or generating a new secret with `openssl rand -base64 32`)
  fixed it.
- **A `DB_URL` from another computer can point to the wrong port**
  ("Connection to localhost:5433 refused"). Leaving `DB_URL` unset uses the
  default port 5432, which matches the README's `docker run`.
- **`No JTA platform available` in the startup log is normal** (an INFO
  message from Hibernate), but looks like an error to a new member.
- **`.vscode/` is not in `.gitignore`** (only `.idea/` is). VS Code's Java
  extension creates `.vscode/settings.json`, which git then shows as a new
  file.

## Conclusion

Local setup works on Linux. The README steps are correct in substance, but a
new Linux member needs the additions above; they are proposed as README
updates in the same PR as this log.
