# GitHub Project board and branch protection workarounds

- **Date:** 2026-09-21, 10:30-13:40 (research to rollout to the group)
- **Status:** Accepted
- **Related issues:** none

## Situation

Two blockers surfaced while setting up the group's workflow. First, the group's admin access on the repository is not enough to create a GitHub Project linked to it; that needs the organization owners to either create one or allow members to. Second, GitHub's rules for protecting `main` (blocking direct pushes) are not available on a private repository without a paid plan, so `main` cannot be locked down yet. Both are waiting on input from Klas and the project owners, and the group wants to start working now rather than wait idle.

## Decision

Two temporary workarounds, rolled out to the group the same day:

1. **Project board:** use a private GitHub Project, owned personally rather than linked to the repository, to hold the issues and track status. Issues are added to it individually rather than through the repository link.
2. **Branch protection:** use a `develop` branch as a stand-in. Everyone branches off `develop` and merges back into it through a pull request; nobody pushes or commits directly to `main`.

## Alternatives considered

- Making the repository public, so free branch protection would apply. Rejected: This decision needs approval from the project owner or supervisor.
- Paying for a GitHub plan that includes branch protection on a private repo. Not something the group can decide on its own; left for the owners.

## Consequences

- The private Project isn't linked to the repository, so new issues don't appear on it automatically; each one has to be added by hand (or through the Project's auto-add workflow, if that turns out to work for a personal Project and an organization repo).
- Nothing on GitHub enforces the `develop` workaround; it depends on everyone following it. A pull request into `develop` also doesn't auto-close its issue the way one into `main` would, since `main` is still the repository's default branch; issues are closed by hand instead.
- Once the owners resolve Project access and branch protection, the private board can be copied into the organization and linked to the repo, and `main` can be protected properly (or `develop` made the default branch in the meantime).
