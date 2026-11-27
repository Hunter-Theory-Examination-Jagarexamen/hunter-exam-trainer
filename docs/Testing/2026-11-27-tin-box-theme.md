# Test: Tin-box theme foundation (#259)

- **Date:** 2026-11-26 to 2026-11-27
- **Branch / commit:** feature/259-tin-box-theme (09d35b5)
- **Related issues:** #259; follow-ups for the login/registration and Hem redesign

## What we tested and why

#259 gives the whole app the tin-box look (dark green lacquer, brass details,
cream text, Cinzel and EB Garamond) and moves every colour into
`styles/variables.css`. It is mostly CSS, so most of the testing is manual: every
page must still work and read well on a phone and on a computer. The few code
changes were tested automatically:

- `components/layout/pageSections.ts` (new): which section a route belongs to,
  used by `Layout.tsx` to give the cards on that page the section colour.
- `role="alert"` added to six load error messages (Hem, Statistik, Profil,
  Quiz subjects, mock exam start), and the auth success/error lines moved from
  inline styles to CSS classes.
- `types/practiceCards.ts`: the card field `iconVariant` (a colour name) became
  `mode` (`subject`, `random`, `exam`).
- `RecentActivityList.tsx`: the frame around recent activity was closed after
  the header row, so the rows sat outside it; it now wraps all rows.

## How

1. Added `__tests__/components/layout/pageSections.test.ts` (4 tests): each
   section's main route, routes below a section, pages without a section, and a
   route that only starts with the same letters (`/administration`).
2. Ran `npx vitest run` after every step that changed a TS/TSX or JSON file.
3. Ran `npm run lint` and `npm run build` on the finished branch.
4. Checked in the running app (`npm run dev`, Firefox), on a computer and in the
   responsive view at 393 × 851 (Pixel 5), as admin:
   - Login, registration, forgot password: fields, labels, links, the primary
     button, the error line after a failed login, and the "session expired"
     notice.
   - Header and sidebar, including the burger menu on the phone.
   - Hem: stat cards and the recent activity table.
   - Quiz page (three mode cards in their own colours, stacked on the phone),
     a subject round and a random round: selected, right and wrong answers,
     the pinned button card, the report link, and the result after a round.
   - Mock exam: start page, questions, bookmarks, the overview grid, the
     "Lämna in provet?" dialog, and the result page ("Ej godkänt").
   - Statistik: cards, the chart (line, axes, tooltip), the weeks dropdown and
     the subject cards.
   - Profil: user card, change password form, result sharing checkboxes and
     the danger zone.
   - Admin: subjects (list and edit form), question bank, participants list and
     one participant's detail page, feedback inbox.
   - Feedback dialog and terms dialog, opened from several pages (they stay
     green everywhere).
   - Server banner with the backend stopped: "starting" (light amber) and,
     after about 90 s, "unreachable" (wine), and the load error boxes on the
     pages behind it.
5. Checked contrast with a script (WCAG formula) for every text colour on every
   background it is used on.

## Result

- Automatic tests: 47 files, 333 tests passed, including the 4 new ones
  (after merging `develop` with the revert of #258, which removed an empty test
  file that had failed every run).
- `npm run lint`: 6 errors, 3 warnings, all in code from before this branch
  (same total as the last check on `develop`).
- `npm run build`: passes. The fonts are bundled as files in `dist/assets`, no
  requests to Google. The build warns that the main JS chunk is just over
  500 KB; #259 adds almost no JavaScript, so this was already close and is
  noted for later.
- Contrast: cream text 7.7:1 or more on every card colour, muted text 5.2:1 or
  more, links 5.7:1 or more, dark text on the brass buttons 6:1 or more, danger
  red 4.5:1 or more. Large Cinzel headings in brass 6:1 or more.
- Manual: all pages above work and read correctly on both sizes. Issues found
  while testing were fixed on the branch (box-in-box login fields, thin button
  text, unreadable disabled buttons, weak right/wrong answer colours, text
  drowning on the admin cards, small tap targets in the participants list,
  white danger zone because Vite missed a file change, fixed by restarting
  `npm run dev`).
- Found but not part of #259 (behaviour, not looks): the mock exam shows a
  "not saved" message for a moment on every answer (from #224); practice could
  check the answer on the first pick instead of "Kontrollera svar"; the report
  link could be a real button.
- Offline: the installed app's cache stores JS, CSS and HTML but not the font
  files, so text falls back to Georgia if the app is opened offline before the
  fonts were ever loaded.

## Conclusion

The theme is in place: one place for colours (`variables.css`), role names
instead of colour names, section colours that each page's cards follow, and
readable text on every page on a phone and on a computer. The redesign of the
login/registration page and of Hem builds on these variables in their own
issues.
