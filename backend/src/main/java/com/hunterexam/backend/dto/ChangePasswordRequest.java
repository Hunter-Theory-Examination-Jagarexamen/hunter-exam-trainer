package com.hunterexam.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request data used while changing the user's password.
 */
@Getter
@Setter
@NoArgsConstructor
public class ChangePasswordRequest {

    private String currentPassword;
    private String newPassword;
}
