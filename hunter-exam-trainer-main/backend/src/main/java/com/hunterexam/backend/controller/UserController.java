package com.hunterexam.backend.controller;

import com.hunterexam.backend.dto.ChangePasswordRequest;
import com.hunterexam.backend.dto.UserResponse;
import com.hunterexam.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for the currently authenticated user.
 * <p>
 * Provides endpoints for retrieving user profile information
 * and changing the user's password.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
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

        String email = authentication.getName();

        return userService.getUserInfo(email);
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
            @RequestBody ChangePasswordRequest request
    ) {

        String email = authentication.getName();

        try {
            userService.changePassword(
                    email,
                    request.getCurrentPassword(),
                    request.getNewPassword()
            );
            return ResponseEntity.ok("Password changed successfully");
        }
        catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }
}
