package com.hunterexam.backend.config;

import com.hunterexam.backend.entity.Question;
import com.hunterexam.backend.entity.Role;
import com.hunterexam.backend.entity.Subject;
import com.hunterexam.backend.entity.User;
import com.hunterexam.backend.repository.QuestionRepository;
import com.hunterexam.backend.repository.SubjectRepository;
import com.hunterexam.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Loads initial subjects and questions into the database when the application starts.
 * <p>
 * Subjects and questions are stored as JSON files in the application's resources.
 * Data is only loaded when the corresponding database table is empty.
 * <p>
 * Also seeds a single admin user on first startup, so admin-only functionality
 * can be tested without manual database edits.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final SubjectRepository subjectRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    public DataInitializer(
            SubjectRepository subjectRepository,
            QuestionRepository questionRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            ObjectMapper objectMapper
    ) {
        this.subjectRepository = subjectRepository;
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) throws Exception {

        // Load subjects from the JSON file when the subject table is empty.
        if (subjectRepository.count() == 0) {

            InputStream subjectFile =
                    new ClassPathResource("data/subjects.json").getInputStream();

            List<Subject> subjects = objectMapper.readValue(
                    subjectFile,
                    new TypeReference<List<Subject>>() {}
            );

            subjectRepository.saveAll(subjects);
            System.out.println("Subjects loaded: " + subjects.size());
        }

        // Load questions from the JSON file when the question table is empty.
        if (questionRepository.count() == 0) {

            InputStream questionFile =
                    new ClassPathResource("data/questions.json").getInputStream();

            List<Map<String, Object>> questionData = objectMapper.readValue(
                    questionFile,
                    new TypeReference<List<Map<String, Object>>>() {}
            );

            // Create a lookup map so each question can be linked to its subject.
            Map<String, Subject> subjectsByName = subjectRepository.findAll()
                    .stream()
                    .collect(Collectors.toMap(
                            Subject::getName,
                            subject -> subject
                    ));

            List<Question> questions = questionData.stream()
                    .map(data -> {

                        Question question = new Question();

                        question.setQuestionText((String) data.get("questionText"));
                        question.setOptionA((String) data.get("optionA"));
                        question.setOptionB((String) data.get("optionB"));
                        question.setOptionC((String) data.get("optionC"));
                        question.setOptionD((String) data.get("optionD"));
                        question.setCorrectAnswer((String) data.get("correctAnswer"));
                        question.setExplanation((String) data.get("explanation"));

                        // Find the subject using the subject name from the JSON data.
                        String subjectName = (String) data.get("subject");
                        Subject subject = subjectsByName.get(subjectName);

                        if (subject == null) {
                            throw new IllegalArgumentException("Subject not found: " + subjectName);
                        }

                        question.setSubject(subject);

                        return question;
                    })
                    .toList();

            questionRepository.saveAll(questions);
            System.out.println("Questions loaded: " + questions.size());
        }

        // Seed a single admin user on first startup.
        if (userRepository.findByEmail(adminEmail).isEmpty()) {

            User admin = new User();
            admin.setFullName("Admin");
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole(Role.ADMIN);
            admin.setCreatedAt(LocalDateTime.now());

            userRepository.save(admin);
            log.info("Admin user created: {}", adminEmail);
        }
    }
}