# Java version for the backend: Java 25

- **Date:** 2026-10-01
- **Status:** Accepted
- **Related issues:** #62 (PR #80)

## Situation

The backend has targeted Java 25 since the previous team (`java.version` in
`pom.xml`, README setup instructions). A recent change on `develop` switched
the project to Java 21: `pom.xml`, the Docker images and the README, plus a
Maven Enforcer rule that stops the build on any JDK other than 21. Everyone
with JDK 25 installed could no longer build the backend. The Project Plan
doesn't require a specific Java version.

## Decision

The group voted in the group chat for **Java 25**. Everyone develops with JDK 25,
and the project targets Java 25.

## Alternatives considered

- **Java 21:** also a long-term support (LTS) version, and the current code
  builds on it. Not chosen: the group and the existing setup already use 25.
- **Target Java 21 but allow any newer JDK** (enforcer range `[21,)`): would
  let both JDK 21 and 25 build. Not chosen, to keep one version for everyone.

## Consequences

- Revert on `develop` through a PR: `java.version` back to 25, the enforcer
  rule removed or set to Java 25, Docker images back to `eclipse-temurin:25`,
  and the README setup section back to Java 25.
- Changing the Java version in the future is a group decision, not part of a
  feature PR.
