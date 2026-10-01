# Staging Deployment Decision

**Date:** 2026-09-29
**Type:** Decision
**Issues:** #57, #47

## Context

Issue #47 (verify installable PWA on a real mobile device) requires HTTPS — `localhost` doesn't work for PWA install on phones. We needed a publicly hosted version of the app to test on mobile.

A previous decision made the database URL configurable via env var, enabling the same code to run in both local and deployed environments.

## Decision

Temporary deploy the app to free-tier hosting for staging:

- **Backend** → Render (Docker container, free tier, auto-deploys from `develop`)
- **Frontend** → Vercel (Vite static site, free tier, auto-deploys from the standalone frontend repo's default branch)
- **Database** → existing Neon instance (same DB as local dev, per team decision)
- **Configuration** → all secrets and env-specific values via environment variables on each platform

## URLs

- Backend: https://hunter-exam-trainer-bak.onrender.com
- Frontend: https://hunter01-kappa.vercel.app

## Rationale

- **Unblocks mobile testing (#47)** — HTTPS is now available
- **No separate branch needed** — `develop` runs everywhere, differentiated only by env vars
- **Local dev unchanged** — localhost still works with fast backend restarts

## Consequences

- **Render free tier sleeps after inactivity** — first request takes 30–60 s. Acceptable for staging.
- **Same database as local dev** — mobile testing creates data in the dev DB. If this becomes noisy, consider a separate Neon instance for staging.
- **Temporary frontend split** — Vercel is currently deployed from a standalone copy of the frontend repo because the main repo is org-owned and requires org-level approval for Vercel. Consolidation is a follow-up.

## Gotchas discovered during setup

- **Lombok annotation processing** needs explicit configuration in `maven-compiler-plugin` for Docker builds (works in IDE, fails in clean container).
- **Vite bakes `VITE_*` env vars at build time** — changing env vars requires a fresh build, and the browser caches old bundles aggressively. Always test in a fresh Incognito window.
- **Env vars in Vercel must be checked for all environments** (Production, Preview, Development) or preview deployments will use the fallback.

## Follow-ups

1. Consolidate frontend deployment to the main `hunter-exam-trainer` repo once org approves Vercel.
2. Consider adding `render.yaml` and `vercel.json` project descriptors so the setup is reproducible.
3. Consider separate Neon database for staging if dev data mixing becomes a problem.
4. Add HikariCP settings (`max-lifetime`, `keepalive-time`) to handle Neon's idle connection timeouts.