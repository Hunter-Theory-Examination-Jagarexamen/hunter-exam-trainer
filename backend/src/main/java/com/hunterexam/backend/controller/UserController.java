package com.hunterexam.backend.controller;

import com.hunterexam.backend.dto.ChangePasswordRequest;
import com.hunterexam.backend.dto.DeleteAccountRequest;
import com.hunterexam.backend.dto.ResultSharingPreferencesResponse;
import com.hunterexam.backend.dto.UpdateResultSharingPreferencesRequest;
import com.hunterexam.backend.dto.UserResponse;
import com.hunterexam.backend.service.AccountDeletionService;
import com.hunterexam.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for the currently authenticated user.
 * <p>
 * Provides endpoints for retrieving user profile information,
 * changing the user's password, managing result sharing choices,
 * and deleting the user's own account.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final AccountDeletionService accountDeletionService;

    public UserController(UserService userService,
                          AccountDeletionService accountDeletionService) {
        this.userService = userService;
        this.accountDeletionService = accountDeletionService;
    }


    /**
     * Returns the profile information of the currently authenticated user.
     * <p>
     * The user's email is obtained from the JWT authentication context,
     * so the client does not need to provide a user ID.
     *
     * @param authentication authentication information from Spring Security
     * @return information about the authenticated user
     */
    @GetMapping("/me")
    public UserResponse getUserInfo(Authentication authentication) {
        return userService.getUserInfo(authentication.getName());
    }


    /**
     * Changes the password of the currently authenticated user.
     * <p>
     * The current password is verified before the new password is saved.
     * If the current password is incorrect, a bad request response is returned.
     *
     * @param authentication authentication information from Spring Security
     * @param request current and new password provided by the client
     * @return success message or an error message
     */
    @PutMapping("/me/password")
    public ResponseEntity<String> changePassword(
        Authentication authentication,
        @RequestBody ChangePasswordRequest request) {
        try {
            userService.changePassword(
                authentication.getName(),
                request.getCurrentPassword(),
                request.getNewPassword());
            return ResponseEntity.ok("Password changed successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Returns which of the authenticated user's results are shared with course
     * leaders (#125). The owner comes from the JWT, so a student can only ever
     * read their own choices.
     *
     * @param authentication authentication information from Spring Security
     * @return the current sharing preferences
     */
    @GetMapping("/me/result-sharing")
    public ResultSharingPreferencesResponse getResultSharing(Authentication authentication) {
        return userService.getResultSharing(authentication.getName());
    }


    /**
     * Replaces the authenticated user's result sharing choices. Both flags are
     * required in the body; the owner is taken from the JWT and never from the
     * request, so no student can change another student's preferences.
     *
     * @param authentication authentication information from Spring Security
     * @param request         the new sharing preferences
     * @return the persisted sharing preferences
     */
    @PutMapping("/me/result-sharing")
    public ResultSharingPreferencesResponse updateResultSharing(
            Authentication authentication,
            @RequestBody @Valid UpdateResultSharingPreferencesRequest request) {
        return userService.updateResultSharing(authentication.getName(), request);
    }


    /**
     * Deletes the authenticated user's own account and all personal data
     * (attempts, attempt questions, subject stats). Feedback is kept without
     * the sender link. The current password is required as re-authentication.
     * <p>
     * The client must discard its stored token after a successful call —
     * the token is rejected from the next request on.
     */
    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMe(Authentication authentication,
                         @RequestBody @Valid DeleteAccountRequest request) {
        accountDeletionService.deleteOwnAccount(
            authentication.getName(),
            request.password());
    }
}
