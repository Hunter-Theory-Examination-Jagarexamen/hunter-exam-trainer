package com.hunterexam.backend.config;

import com.hunterexam.backend.entity.Role;
import com.hunterexam.backend.entity.User;
import com.hunterexam.backend.repository.QuestionRepository;
import com.hunterexam.backend.repository.SubjectRepository;
import com.hunterexam.backend.repository.UserRepository;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit test class for verifying the behaviour of DataInitializer regarding admin management.
 */
public class DataInitializerTest {

    private SubjectRepository subjectRepository;
    private QuestionRepository questionRepository;
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private ObjectMapper objectMapper;
    private DataInitializer dataInitializer;

    @BeforeEach
    void setUp() {
        subjectRepository = mock(SubjectRepository.class);
        questionRepository = mock(QuestionRepository.class);
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        objectMapper = mock(ObjectMapper.class);

        dataInitializer = new DataInitializer(
                subjectRepository,
                questionRepository,
                userRepository,
                passwordEncoder,
                objectMapper
        );
    }

    @Test
    void testDoesNotModifyExistingAdminsWhenMultipleAdminsExist() throws Exception {
        User admin1 = new User();
        admin1.setId(1L);
        admin1.setEmail("admin1@example.com");
        admin1.setRole(Role.ADMIN);

        User admin2 = new User();
        admin2.setId(2L);
        admin2.setEmail("admin2@example.com");
        admin2.setRole(Role.ADMIN);

        when(subjectRepository.count()).thenReturn(1L);
        when(questionRepository.count()).thenReturn(1L);
        when(userRepository.findAll()).thenReturn(List.of(admin1, admin2));

        java.lang.reflect.Field emailField = DataInitializer.class.getDeclaredField("adminEmail");
        emailField.setAccessible(true);
        emailField.set(dataInitializer, "newadmin@example.com");

        java.lang.reflect.Field passwordField = DataInitializer.class.getDeclaredField("adminPassword");
        passwordField.setAccessible(true);
        passwordField.set(dataInitializer, "securepassword");

        dataInitializer.run();

        verify(userRepository, never()).save(any(User.class));
    }
}
