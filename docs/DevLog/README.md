# Development log

A short, running record of what the team decided and why. It lets new team members, supervisors and the project owners follow the project's history without digging through chats or commits.

## What goes where

- `decisions/`: one file per decision about design, technology or how we work.
- `meetings/`: one file per meeting, with the decisions made and the follow-up work.
- `tests/`: one file per code test or experiment, with the result and what we concluded.
- `_templates/`: empty copies of the three formats. Copy one to start a new entry.

## Adding an entry

1. Copy the matching template from `_templates/` into the right folder.
2. Name it `YYYY-MM-DD-short-title.md`, for example `2026-09-21-develop-branch.md`. Date first, so the folder sorts itself in date order.
3. Fill it in. Keep it short; one page is plenty.
4. Optional: link related issues by number (for example #41), and link decisions from the meeting where they were made.
5. Add it through a branch and pull request, like any other change.

## Rules of thumb

- One decision per file, so each one can be found and linked on its own.
- Write for someone who was not in the room.
- Fixing typos, links and small inaccuracies in an old entry is fine. If the decision itself changes, don't rewrite the entry. Add a new decision file and set the old one's status to "Replaced by" (with a link if you like).
- 