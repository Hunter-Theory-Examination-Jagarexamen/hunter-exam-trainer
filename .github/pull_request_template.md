## What changed

<!--
Describe the change in a few lines.
If this PR finishes an issue, add "Closes #<number>" (e.g. Closes #38). GitHub then closes the issue when the PR is merged.
If it only relates to an issue, write "Related to #<number>" instead. If there is no issue, leave it out.
-->

## How I tested

<!-- What you did in the running app, and what you saw. -->

## Database changes

<!--
Write "None" if no entity or table changed.
Otherwise explain what changed and what teammates need to do with their existing local database.
Example: "User.role is now required. Old users with an empty role must be updated,
or drop and recreate your local database."
-->

## Definition of Done

- [ ] Tested in the running app, not just the endpoint
- [ ] If an API request or response changed, every caller of it is updated (search the endpoint path in frontend and backend)
- [ ] Error cases checked in the browser: the user actually sees a clear message
- [ ] README updated if setup changed (e.g. new environment variables)
- [ ] If this PR changes the database (new or changed tables, columns or required fields), I have described in the Database changes section what others must do so the app still starts and works with the data they already have locally
- [ ] "How I tested" is filled in
