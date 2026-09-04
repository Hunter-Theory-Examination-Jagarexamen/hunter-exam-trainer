package com.hunterexam.backend.config;

import com.hunterexam.backend.entity.Question;
import com.hunterexam.backend.entity.Subject;
import com.hunterexam.backend.repository.QuestionRepository;
import com.hunterexam.backend.repository.SubjectRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class DataInitializer implements CommandLineRunner {

    private final SubjectRepository subjectRepository;
    private final QuestionRepository questionRepository;
    private final ObjectMapper objectMapper;

    public DataInitializer(
            SubjectRepository subjectRepository,
            QuestionRepository questionRepository,
            ObjectMapper objectMapper
    ) {
        this.subjectRepository = subjectRepository;
        this.questionRepository = questionRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) throws Exception {

        // Load subjects if the database is empty
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

        // Load subjects if the database is empty
        if (questionRepository.count() == 0) {

            InputStream questionFile =
                    new ClassPathResource("data/questions.json").getInputStream();

            List<Map<String, Object>> questionData = objectMapper.readValue(
                    questionFile,
                    new TypeReference<List<Map<String, Object>>>() {}
            );

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
    }
}
