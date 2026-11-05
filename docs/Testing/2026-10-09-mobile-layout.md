# Test: Mobile layout login page and navigation

- **Date:** 2026-10-09
- **Branch / commit:** `feature/82-mobile-layout-login-nav`
- **Related issues:** #82

## What we tested and why

Verified that the app is usable on small phone screens (~360px wide),
specifically:

1. The login page fits without horizontal scrolling.
2. Navigation is one-hand-friendly (burger menu drawer instead of
   a horizontally-scrolling row).

## How

1. Started backend (`./mvnw spring-boot:run`) and frontend (`npm run dev`).
2. Opened Chrome DevTools → device toolbar → set viewport to **360×800**
   (Galaxy S8) and **375×667** (iPhone SE).
3. Navigated to `/login` — checked that the page fits without sideways
   scrolling.
4. Logged in as an ADMIN user.
5. Tapped the hamburger icon in the header — drawer slides in from the
   left with a backdrop overlay.
6. Tapped a nav link (e.g., Statistics) — drawer closes and navigates.
7. Tapped the backdrop — drawer closes without navigating.
8. Navigated programmatically (browser back button) — drawer closes.
9. Widen the viewport to desktop (> 768px) — burger hidden, sidebar
   always visible.
10. Repeated step 2–3 on a real phone using `npm run dev -- --host`
    (same Wi-Fi).

## Result

All checks passed:

- ✅ Login page fits at 360px and 375px without horizontal scroll
- ✅ Register page fits at 360px and 375px
- ✅ Hamburger button visible only on mobile (< 768px)
- ✅ Drawer slides in smoothly, backdrop visible
- ✅ Tapping a nav link closes the drawer and navigates
- ✅ Tapping the backdrop closes the drawer
- ✅ Route change (back button) closes the drawer
- ✅ Desktop layout unchanged burger hidden, sidebar always visible

## Conclusion

Mobile layout now works on the smallest phone sizes we target. No
horizontal scrolling, one-hand-friendly navigation via burger menu.
Desktop is unaffected.

## Notes

- Breakpoint: `@media (max-width: 768px)` for the drawer, plus
  `@media (max-width: 400px)` for extra-small phones on auth pages.
- The admin nav links (Admin, Learner Progress) work inside the
  drawer too.
- Screenshots: [link to the PR #100 comment or attach here]
- Out of scope (filed separately): cosmetic issue where subject name
  and percentage collide on the Learner Progress card.