# Test: Account deletion (GDPR right to erasure)

- **Date:** 2026-11-10
- **Branch / commit:** `feature/194-account-deletion`
- **Related issues:** #194

## What we tested and why

Verified that users can permanently delete their own account and all associated personal data, as required by GDPR. We also verified that admins can delete student accounts through a new admin endpoint, but cannot delete other admins.

Specifically:

1. A student can delete their own account by providing their current password. A wrong password deletes nothing and shows an error.
2. An admin can delete their own account, but not if they are the last remaining admin.
3. An admin can delete a student account via the new endpoint, but not an admin account.
4. Deleting a user removes their attempts, answers, and subject statistics atomically, while keeping their feedback but setting the sender link to `null`.
5. A JWT issued before deletion is immediately rejected on the next request.
6. The frontend "Danger Zone" renders correctly on the Profile page, handles password confirmation, and redirects to the login page with a success banner after deletion.

## How

### Automated

**Backend tests (`AccountDeletionServiceTest`):**
- `deleteOwnAccount_withWrongPassword_deletesNothing`
- `deleteOwnAccount_withCorrectPassword_deletesAllPersonalData`
- `deleteOwnAccount_keepsFeedbackButRemovesSenderLink`
- `deleteOwnAccount_whenLastAdmin_isRejected`
- `deleteOwnAccount_whenNotLastAdmin_succeeds`
- `deleteStudentAsAdmin_deletesStudent`
- `deleteStudentAsAdmin_whenTargetIsAdmin_isRejected`
- `deleteStudentAsAdmin_whenTargetMissing_isRejected`

**Backend token invalidation test (`DeletedUserTokenTest`):**
- `tokenIssuedBeforeDeletion_isRejectedAfterDeletion`

Run: `./mvnw test -Dtest="AccountDeletionServiceTest,DeletedUserTokenTest"`

**Frontend tests (`DangerZone.test.tsx`):**
- `reveals the password field only after clicking the delete button`
- `wrong password shows the server message and does not log out`
- `correct password navigates to logout?deleted=1`
- `cancel clears the form without calling the API`

Run: `npx vitest run src/__tests__/components/DangerZone.test.tsx`

### Manual

- Logged in as a student, opened `/profile`, scrolled to the "Danger Zone".
- Clicked "Delete account", entered the wrong password → inline error shown, nothing deleted.
- Entered the correct password → redirected to `/login?deleted=1` with the "Your account has been deleted." banner. URL param cleaned up after refresh.
- Logged in as an admin, verified self-delete works when another admin exists.
- Logged in as the last remaining admin, verified self-delete is rejected with a `409 Conflict`.
- Sent `DELETE /api/admin/users/{id}` as an admin on a student → `204 No Content`.
- Sent the same request on another admin → `403 Forbidden`.
- Verified via database that all `attempts`, `attempt_questions`, and `subject_stats` for the deleted user were gone, and that `feedback.sender_id` was set to `null`.
- Grabbed a JWT for a user, deleted the account, then re-used the same token to call `/api/users/me` → `401 Unauthorized`.

## Results

All 9 backend tests passed. All 4 frontend tests passed. Manual verification matched expectations.

## Known limitations / follow-ups

- Delete-error messages (wrong password, last admin) are English even on the Swedish UI, consistent with the rest of the app. Tracked as a follow-up to add an error `code` field to `GlobalExceptionHandler`.
- No admin UI button was added; admin deletion happens via API for now (blocked by #156).
- Google-login account deletion is out of scope per the issue.
