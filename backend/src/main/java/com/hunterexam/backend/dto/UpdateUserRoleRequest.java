package com.hunterexam.backend.dto;

import com.hunterexam.backend.entity.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Request body for {@code PUT /api/admin/users/{id}/role}. */
@Getter
@Setter
@NoArgsConstructor
public class UpdateUserRoleRequest {

    @NotNull(message = "Role is required")
    private Role role;
}
