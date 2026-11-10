package com.hunterexam.backend.security;

import com.hunterexam.backend.entity.Role;
import com.hunterexam.backend.entity.User;
import com.hunterexam.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.ActiveProfiles;


import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Proves that a JWT issued before the account was deleted is rejected from the
 * next request on, as required by #194. Without the
 * {@link UserExistsJwtAuthenticationConverter} this test fails, because the
 * plain {@code NimbusJwtDecoder} validates signature and expiry only.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class DeletedUserTokenTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;

    @Test
    void tokenIssuedBeforeDeletion_isRejectedAfterDeletion() throws Exception {
        User user = new User();
        user.setEmail("gone@x.se");
        user.setFullName("Gone User");
        user.setPassword(passwordEncoder.encode("x"));
        user.setRole(Role.STUDENT);
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());

        // Token works while the user exists.
        mockMvc.perform(get("/api/users/me")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());

        // Delete the user directly this is what the service does in production.
        userRepository.deleteById(user.getId());
        userRepository.flush();

        // Same token, same request: now rejected.
        mockMvc.perform(get("/api/users/me")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isUnauthorized());
    }
}
