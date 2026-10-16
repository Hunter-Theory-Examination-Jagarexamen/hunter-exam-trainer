# Meeting: Status update and admin interface issues

- **Date:** 2026-10-01, 13:00
- **Attendees:** Fadi Alaraj, Md Abdus Sikdar, Maja Blom, Hans Malmefjäll

## Topics discussed

Status update: what each member is working on.

## Decisions made

**Split the admin interface issue (#32) into smaller issues**, so work can
start now and each part fits in one pull request:

1. Admin area: access and question overview (new text for #32)
2. Admin: add, edit and delete questions (uses the endpoints from #30 / PR #71)
3. Manage subjects (categories), using the endpoints from #31 (PR #74)

Images in questions are handled in their own issues ("Add optional image to
questions", then image upload), so none of the admin parts waits for them.

## Follow-up work

- Update #32 and create the issues for parts 2 and 3.
