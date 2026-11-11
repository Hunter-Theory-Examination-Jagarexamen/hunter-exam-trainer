# Admin deletes student from detail page #<new-issue-number>

## Frontend Tests
- `LearnerDetailPage.test.tsx` PASS (13 tests total)
    - **Existing coverage (7)**: empty state, mock exam summary, practice subjects, both sections, "Not passed", heading/email/back-link, 404 not-found.
    - **New coverage (6)**:
        - delete button visible for a student
        - delete button hidden for an admin
        - confirmation dialog shows the student's name
        - cancel closes the dialog without calling the API
        - confirm calls `DELETE /api/admin/users/{id}` and navigates to `/admin/learners` with `state: { deleted: fullName }`
        - API failure shows the server error inside the dialog and does not navigate

Run: `cd frontend && npx vitest run src/__tests__/pages/Admin/LearnerDetailPage.test.tsx`

## Manual Verification
- [ ] Admin → Elever → click a student → scroll to "Riskzon" at the bottom
- [ ] Click "Ta bort studentkonto" → confirmation dialog appears, shows the student's name, warns the deletion is permanent
- [ ] Cancel → dialog closes, no change
- [ ] Confirm → redirected to `/admin/learners`, success banner "X:s konto har tagits bort." shown, student gone from the list
- [ ] Open an admin's detail page → no "Riskzon" section visible
- [ ] Simulate a backend failure (stop backend or use dev tools) → error shows in the dialog, no navigation
