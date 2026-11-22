package com.hunterexam.backend.security;

import com.hunterexam.backend.entity.Role;
import com.hunterexam.backend.entity.User;
import com.hunterexam.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Proves that a role change is effective from the next request on: the
 * authorities of a request are built from the role in the database, not from
 * the {@code role} claim of the (one hour long living) JWT. Without that a
 * demoted administrator would keep using admin endpoints until their token
 * expires.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RoleChangeSecurityTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;

    private User saveUser(String email, Role role) {
        User user = new User();
        user.setEmail(email);
        user.setFullName(email);
        user.setPassword(passwordEncoder.encode("secret"));
        user.setRole(role);
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    private String tokenFor(User user) {
        return jwtService.generateToken(user.getEmail(), user.getRole().name());
    }

    private void changeRole(User actingAdmin, User target, Role newRole) throws Exception {
        mockMvc.perform(put("/api/admin/users/{id}/role", target.getId())
                        .header("Authorization", "Bearer " + tokenFor(actingAdmin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"" + newRole.name() + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value(newRole.name()));
    }

    @Test
    void studentsCannotChangeRoles() throws Exception {
        User student = saveUser("student-role@example.com", Role.STUDENT);
        User target = saveUser("target-role@example.com", Role.STUDENT);
        String token = tokenFor(student);

        mockMvc.perform(put("/api/admin/users/{id}/role", target.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"ADMIN\"}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/admin/users/{id}/role", target.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"ADMIN\"}"))
                .andExpect(status().isUnauthorized());

        // The rejected calls did not change anything.
        assertEquals(Role.STUDENT, userRepository.findById(target.getId()).orElseThrow().getRole());
    }

    @Test
    void demotedAdministratorLosesAdminAccessWithTheTokenItAlreadyHas() throws Exception {
        User actingAdmin = saveUser("acting-admin@example.com", Role.ADMIN);
        User victim = saveUser("victim-admin@example.com", Role.ADMIN);
        String staleToken = tokenFor(victim);

        mockMvc.perform(get("/api/admin/learners")
                        .header("Authorization", "Bearer " + staleToken))
                .andExpect(status().isOk());

        changeRole(actingAdmin, victim, Role.STUDENT);

        // Same token, same request: now rejected, without waiting for expiry.
        mockMvc.perform(get("/api/admin/learners")
                        .header("Authorization", "Bearer " + staleToken))
                .andExpect(status().isForbidden());

        // The token still identifies the user; only the rights changed.
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + staleToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(victim.getEmail()))
                .andExpect(jsonPath("$.role").value("STUDENT"));
    }

    @Test
    void promotedStudentGainsAdminAccessAfterLoggingInAgain() throws Exception {
        User actingAdmin = saveUser("acting-admin-2@example.com", Role.ADMIN);
        User student = saveUser("promoted-student@example.com", Role.STUDENT);
        String oldToken = tokenFor(student);

        mockMvc.perform(get("/api/admin/learners")
                        .header("Authorization", "Bearer " + oldToken))
                .andExpect(status().isForbidden());

        changeRole(actingAdmin, student, Role.ADMIN);

        // A fresh login issues a token with the new role claim …
        String freshToken = jwtService.generateToken(student.getEmail(), Role.ADMIN.name());
        mockMvc.perform(get("/api/admin/learners")
                        .header("Authorization", "Bearer " + freshToken))
                .andExpect(status().isOk());

        // … and even the token from before the promotion works, because the
        // authorities are read from the database on every request.
        mockMvc.perform(get("/api/admin/learners")
                        .header("Authorization", "Bearer " + oldToken))
                .andExpect(status().isOk());
    }
}
