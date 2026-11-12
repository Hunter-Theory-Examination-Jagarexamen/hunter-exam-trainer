# Rename the app and some menu names #222

New app name "JägarQuizzen", menu "Hem" / "Quiz", app users "Deltagare". Text changes only: translation keys keep their names.

## Frontend Tests
- 7 test files updated for the new texts (no new tests: only wording changed):
    - `AdminHome.test.tsx`, `AdminRoutes.test.tsx`, `LearnerProgressPage.test.tsx`: "Deltagare"
    - `SubjectIntegration.test.tsx`: menu link "Quiz"
    - `NotFound.test.tsx`: "Gå till Hem"
    - `PracticeQuestions.test.tsx`, `RandomPractice.test.tsx`: "Fortsätt Quiz", "Tillbaka till Quiz"
- Result: 13 test files, 125 tests PASS

Run: `cd frontend && npx vitest run src/__tests__/pages/Admin src/__tests__/pages/NotFound.test.tsx src/__tests__/pages/Practice src/__tests__/routes/AdminRoutes.test.tsx`

## Manual Verification (Swedish)
- [x] Browser tab and header say "JägarQuizzen"
- [ ] Login, register and password pages show "JägarQuizzen" as the big title (was still the old name in capitals; fixed after the first check)
- [x] Menu shows "Hem" and "Quiz"; the pages' headings match
- [x] Hem page button says "Starta Quiz"
- [x] Quiz page: "Öva per ämne" button "Välj"; "Slumpmässig övning" button "Starta Quiz"
- [ ] Subject list: subject cards say "Starta Quiz"
- [ ] During a round, click a menu item: dialog button says "Fortsätt Quiz"
- [ ] After a random round: back link says "Tillbaka till Quiz"
- [ ] Admin menu and admin start page say "Deltagare"; participant progress page "Deltagarnas framsteg"
- [ ] Participant detail page: delete button "Ta bort deltagarkonto"
- [ ] Feedback page subtitle "Meddelanden och felrapporter från deltagarna."
- [ ] Unknown URL (404): button "Gå till Hem"
- [ ] Terms of Use and Privacy Policy name "JägarQuizzen"

## Manual Verification (English)
- [ ] Switch language: menu "Home", "Quiz", "Participants"; app name stays "JägarQuizzen"

## Not covered here
- The name under the app icon on a phone (PWA manifest) shows after the next deployment; reinstalling the app may be needed.
- The email sender name (`MAIL_FROM` on Render) is changed after the merge.
