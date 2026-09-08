package com.hunterexam.backend.controller;

import com.hunterexam.backend.dto.UserResponse;
import com.hunterexam.backend.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
