# Test: Local setup verified end to end

- **Date:** 2026-09-18
- **Branch / commit:** `main` at `535959b` (before `develop` was created)
- **Related issues:** #7

## What we tested and why

Whether a new team member can run the whole app (backend, database and frontend) locally by following the repo as it was handed over from the previous team. Everything else we build depends on this working.

## How

1. **JDK:** 25 (matches `java.version` in `backend/pom.xml`).
2. **Backend build:** the Maven wrapper, no separate Maven install. In Git Bash use `./mvnw`; `mvnw.cmd` is for PowerShell/cmd. In IntelliJ, right-click `backend/pom.xml` → **Add as Maven Project**, since the `pom.xml` is not in the repo root.
3. **Database:** MySQL 8 in Docker:
   ```
   docker run --name hunter-exam-mysql -e MYSQL_ROOT_PASSWORD=... -e MYSQL_DATABASE=hunter_exam -e MYSQL_USER=... -e MYSQL_PASSWORD=... -p 3306:3306 -d mysql:8
   ```
4. **Environment variables** in the IntelliJ run configuration: `DB_USERNAME` and `DB_PASSWORD` (matching the container), and `JWT_SECRET`.
5. **Frontend:** `npm install`, then `npm run dev`.

## Result

The full stack ran locally: the backend started against MySQL and the frontend loaded in the browser. Notes from the run:

- **`WeakKeyException` at startup:** a short `JWT_SECRET` is rejected. Generate one with `openssl rand -base64 32`.
- `npm install` reports some audit warnings. They don't block anything.

## Conclusion

Local setup works. Issue #7 is recorded as done; reopen it if a problem turns up.

Note: the database was later changed from MySQL to PostgreSQL (#43) and the URL made configurable (#51). The README describes the current setup.
