package com.hunterexam.backend.repository;

import com.hunterexam.backend.entity.Role;
import com.hunterexam.backend.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest(showSql = false)
@ActiveProfiles("test")
class UserRepositoryTests {

    @Autowired UserRepository users;

    @BeforeEach
    void setUp() {
        users.deleteAll();
        saveUser("Active Student", "active-student@example.test", Role.STUDENT, true);
        saveUser("Inactive Student", "inactive-student@example.test", Role.STUDENT, false);
        saveUser("Admin", "admin@example.test", Role.ADMIN, true);
    }

    private void saveUser(String fullName, String email, Role role, boolean passwordLoginEnabled) {
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword("irrelevant-for-counting");
        user.setRole(role);
        user.setPasswordLoginEnabled(passwordLoginEnabled);
        user.setCreatedAt(LocalDateTime.now());
        users.saveAndFlush(user);
    }

    @Test
    void countByRoleCountsStudentsThatCannotUsePasswordLogin() {
        assertEquals(2, users.countByRole(Role.STUDENT));
    }

    @Test
    void countByRoleKeepsAdminsOutOfTheStudentCount() {
        assertEquals(1, users.countByRole(Role.ADMIN));
    }
}
