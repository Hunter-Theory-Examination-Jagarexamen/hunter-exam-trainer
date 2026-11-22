package com.hunterexam.backend.service;

import com.hunterexam.backend.entity.Role;
import com.hunterexam.backend.entity.User;
import com.hunterexam.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Every administrator demotes a different administrator at the same time.
 * Without the lock around the last-administrator check, all of them could read
 * the same count, all pass it, and leave the application without a single
 * administrator. This test is deliberately not {@code @Transactional}: the
 * requests run in their own transactions on their own threads, exactly like
 * concurrent HTTP calls.
 */
@SpringBootTest
@ActiveProfiles("test")
class UserRoleConcurrencyTests {

    @Autowired private UserRoleService userRoleService;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    void concurrentDemotionsNeverRemoveEveryAdministrator() throws Exception {
        // The shared test database also holds users created by other tests and
        // the seeded admin, so everything is put back afterwards.
        Map<Long, Role> originalRoles = new HashMap<>();
        List<User> created = new ArrayList<>();
        ExecutorService pool = null;

        try {
            userRepository.findAll().forEach(user -> originalRoles.put(user.getId(), user.getRole()));

            List<User> admins = currentAdmins();
            if (admins.size() < 2) {
                created.add(saveAdmin("concurrency-extra-admin@example.com"));
                admins = currentAdmins();
            }
            assertTrue(admins.size() >= 2, "the test needs at least two administrators");

            int numberOfAdmins = admins.size();
            pool = Executors.newFixedThreadPool(numberOfAdmins);
            CountDownLatch startLine = new CountDownLatch(1);
            List<Future<?>> attempts = new ArrayList<>();

            for (int i = 0; i < numberOfAdmins; i++) {
                User actingAdmin = admins.get(i);
                User target = admins.get((i + 1) % numberOfAdmins);
                attempts.add(pool.submit(() -> {
                    startLine.await();
                    try {
                        userRoleService.changeRole(target.getId(), Role.STUDENT, actingAdmin.getEmail());
                    } catch (Exception refused) {
                        // The loser of the race (or the one who would leave no
                        // administrator behind) is expected to be refused.
                    }
                    return null;
                }));
            }

            startLine.countDown();
            for (Future<?> attempt : attempts) {
                attempt.get(30, TimeUnit.SECONDS);
            }

            assertTrue(userRepository.countByRole(Role.ADMIN) >= 1,
                    "concurrent demotions must never remove every administrator");
        } finally {
            if (pool != null) {
                pool.shutdownNow();
            }
            restore(originalRoles, created);
        }
    }

    private List<User> currentAdmins() {
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.ADMIN)
                .toList();
    }

    private User saveAdmin(String email) {
        User admin = new User();
        admin.setEmail(email);
        admin.setFullName("Concurrency Admin");
        admin.setPassword(passwordEncoder.encode("secret"));
        admin.setRole(Role.ADMIN);
        admin.setCreatedAt(LocalDateTime.now());
        return userRepository.save(admin);
    }

    private void restore(Map<Long, Role> originalRoles, List<User> created) {
        originalRoles.forEach((id, role) -> userRepository.findById(id).ifPresent(user -> {
            if (user.getRole() != role) {
                user.setRole(role);
                userRepository.save(user);
            }
        }));
        for (User user : created) {
            if (userRepository.existsById(user.getId())) {
                userRepository.delete(user);
            }
        }
        userRepository.flush();
    }
}
