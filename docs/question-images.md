# Question images

Images use existing optional `imageUrl` fields. The admin form sends a complete
HTTP(S) URL or explicit `null` in POST/PUT requests. PUT replaces every question
field: omitted imageUrl also becomes null; an empty string is stored as an empty
string. The form preserves the existing URL when editing unrelated fields.
No upload or hosting infrastructure is included. Q-007 remains open for team
decisions on image hosting, licensing, and upload strategy.

## Database deployment requirement

QuestionRequest allows 1000 characters; the previous JPA mapping defaulted to
varchar(255). Question.imageUrl now declares length 1000, matching the API.
The project has no Flyway/Liquibase migrations and currently configures Hibernate
ddl-auto=update (tests use create-drop). No database configuration was changed.

Before deploying this mapping to an existing PostgreSQL database, the team must
coordinate and verify widening the column. Hibernate schema update may attempt
this at startup; do not deploy assuming the existing column has already widened.
The equivalent explicit SQL, for the default schema/table naming, is:

```sql
ALTER TABLE question ALTER COLUMN image_url TYPE varchar(1000);
```

Confirm the target schema, permissions, and deployment procedure first. This SQL
is documentation for the existing deployment process, not an automatic migration,
and has not been executed against any database. Existing values are preserved by
widening. Verify the deployed column accepts a 1000-character URL before release.
