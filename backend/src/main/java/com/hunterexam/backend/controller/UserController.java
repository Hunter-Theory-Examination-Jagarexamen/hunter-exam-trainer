package com.hunterexam.backend.controller;

import com.hunterexam.backend.dto.ChangePasswordRequest;
import com.hunterexam.backend.dto.UserResponse;
import com.hunterexam.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public UserResponse getUserInfo(Authentication authentication) {

        String email = authentication.getName();

        return userService.getUserInfo(email);
    }

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
