# Contributing

How we work in this repository. Short on purpose: if something here is unclear or outdated, fix it through a PR.

## Workflow

1. Pick an issue from the Project board, or create one for code work.
2. Create a branch off `develop`, named after the type and issue, for example `feature/38-frontend-tests`, `fix/49-exam-contract` or `docs/documentation-update`.
3. Commit on your own branch as often as you like.
4. Open a pull request into `develop`. The PR template shows the Definition of Done checklist.
5. Merge when the checklist is done. Self-merging is allowed: you may merge your own PR yourself, so a review by a teammate is optional. Asking someone to review is still welcome, especially for bigger or risky changes.
6. Delete your branch after merging (GitHub offers this on the merged PR). Don't delete other people's branches without asking them.

## Shared branches only change through PRs

Every change to `develop` and `main` goes through a pull request, even a one-line docs fix. There is no "small enough to commit directly".

`main` is the production branch. It is updated from `develop` when the group agrees a version is tested and ready.

## Definition of Done

Before a PR is merged, the author has:

- tested the change in the running app, not just the endpoint,
- updated every caller if an API request or response changed (search for the endpoint path in both frontend and backend),
- checked error cases in the browser, so the user actually sees a clear message,
- updated the README if setup changed (e.g. new environment variables),
- described database changes that affect existing local databases,
- written a short "How I tested" note in the PR.

## Where things are documented

- Setup and environment variables: [README](README.md)
- Decisions and meeting notes: [docs/DevLog](docs/DevLog/00-README.md)
- Testing and test logs: [docs/Testing](docs/Testing/00-Test-Plan.md)
