# Development log

A short, running record of what the team decided and why. It lets new team members, supervisors and the project owners follow the project's history without digging through chats or commits.

## How it's organized

One flat folder, no subfolders. This file is named `00-README.md` so it always sorts first; every other file is a dated entry, so the folder sorts itself in date order.

**Filename:** `YYYY-MM-DD-Type-short-title.md`, for example `2026-09-25-Decision-mysql-vs-postgres.md`. Type is one of `Meeting`, `Decision` or `Test`. The short title matters: two entries can land on the same date, and the title is what tells them apart.

## Meeting vs. Decision

A **Meeting** entry is the record of a meeting: what was discussed and what was decided. If a decision is made live in a meeting, write it directly into that meeting's "Decisions made" section — it does not need its own file.

A **Decision** entry is for something settled outside a meeting, for example over text between meetings. It gives a decision made asynchronously the same kind of home a meeting-made one already has.

A **Test** entry records a code test or experiment: what was tried, how, and what came of it.

## Adding an entry

1. Recommended: give your AI assistant your notes plus this file (for the naming pattern and the format below), and ask it to write the entry for you. Read it over before committing; you're still responsible for what it says.
2. Copy the matching format below into a new file, named as described above.
3. Fill it in. Keep it short; one page is plenty.
4. Optional: link related issues by number (for example #41).
5. Add it through a branch and pull request, like any other change.

## Rules of thumb

- Write for someone who was not in the room.
- Fixing typos, links and small inaccuracies in an old entry is fine. If the decision itself changes, don't rewrite the entry. Add a new decision file and set the old one's status to "Replaced by" (with a link if you like).

## Formats

### Meeting

```markdown
# Meeting: short title

- **Date:** YYYY-MM-DD
- **Attendees:**

## Topics discussed

Short notes on each topic.

## Decisions made

What was decided, directly here — no separate decision file needed for these.

## Follow-up work

Link each item to an issue (for example #41).
```

### Decision

```markdown
# Title of the decision

- **Date:** YYYY-MM-DD
- **Status:** Proposed / Accepted / Replaced by [link to newer decision]
- **Related issues:** #

## Situation

What problem or question led to this? Two or three sentences.

## Decision

What did we decide?

## Alternatives considered

What else did we look at, and why did we not choose it?

## Consequences

What changes because of this? Is there anything we now have to do or watch out for?
```

### Test

```markdown
# Test: short title

- **Date:** YYYY-MM-DD
- **Branch / commit:**
- **Related issues:** #

## What we tested and why

## How

The steps, so someone else can repeat the test.

## Result

What happened?

## Conclusion

What do we do with the result?
```
