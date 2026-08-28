package com.hunterexam.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterResponse {

    private Long id;
    private String fullName;
    private String email;
    private String role;

    public RegisterResponse(Long id, String fullName, String email, String role) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
    }
}
