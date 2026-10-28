package com.hunterexam.backend.service;

import com.hunterexam.backend.dto.ExamQuestionResponse;
import com.hunterexam.backend.dto.PracticeAnswerResponse;
import com.hunterexam.backend.dto.PracticeStartResponse;
import com.hunterexam.backend.entity.Attempt;
import com.hunterexam.backend.entity.AttemptQuestion;
import com.hunterexam.backend.entity.AttemptType;
import com.hunterexam.backend.entity.Question;
import com.hunterexam.backend.entity.Subject;
import com.hunterexam.backend.entity.SubjectStats;
import com.hunterexam.backend.entity.User;
import com.hunterexam.backend.exception.ConflictException;
import com.hunterexam.backend.exception.NotFoundException;
import com.hunterexam.backend.repository.AttemptQuestionRepository;
import com.hunterexam.backend.repository.AttemptRepository;
import com.hunterexam.backend.repository.QuestionRepository;
import com.hunterexam.backend.repository.SubjectRepository;
import com.hunterexam.backend.repository.SubjectStatsRepository;
import com.hunterexam.backend.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Practice rounds stored per question (#168): start a round, answer one
 * question at a time (checked on the server), finish the round.
 */
@Service
public class PracticeAttemptService {

    private final AttemptRepository attemptRepository;
    private final AttemptQuestionRepository attemptQuestionRepository;
    private final QuestionRepository questionRepository;
    private final SubjectRepository subjectRepository;
    private final SubjectStatsRepository subjectStatsRepository;
    private final UserRepository userRepository;

    public PracticeAttemptService(
            AttemptRepository attemptRepository,
            AttemptQuestionRepository attemptQuestionRepository,
            QuestionRepository questionRepository,
            SubjectRepository subjectRepository,
            SubjectStatsRepository subjectStatsRepository,
            UserRepository userRepository
    ) {
        this.attemptRepository = attemptRepository;
        this.attemptQuestionRepository = attemptQuestionRepository;
        this.questionRepository = questionRepository;
        this.subjectRepository = subjectRepository;
        this.subjectStatsRepository = subjectStatsRepository;
        this.userRepository = userRepository;
    }

    /**
     * Starts a practice round with all questions of one subject.
     * <p>
     * Creates the round and one {@link AttemptQuestion} row per question, then
     * returns the questions without their correct answers.
     *
     * @param subjectId the subject to practise
     * @param email     email of the logged-in user
     * @return the round's id and its questions
     */
    @Transactional
    public PracticeStartResponse start(Long subjectId, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!subjectRepository.existsById(subjectId)) {
            throw new NotFoundException("Subject not found with ID: " + subjectId);
        }

        List<Question> questions = questionRepository.findBySubjectId(subjectId);
        if (questions.isEmpty()) {
            throw new ConflictException("This subject has no questions yet.");
        }

        Attempt attempt = attemptRepository.save(
                new Attempt(user, AttemptType.SUBJECT_PRACTICE, LocalDateTime.now()));

        List<AttemptQuestion> rows = new ArrayList<>();
        for (int position = 0; position < questions.size(); position++) {
            rows.add(new AttemptQuestion(attempt, questions.get(position), position));
        }
        attemptQuestionRepository.saveAll(rows);

        List<ExamQuestionResponse> questionDtos = questions.stream()
                .map(this::toQuestionResponse)
                .toList();

        return new PracticeStartResponse(attempt.getId(), questionDtos);
    }

    /**
     * Checks and saves one answer, and adds it to the student's per-subject totals.
     * <p>
     * The round must belong to the user and still be running, the question must
     * be part of the round and not answered already. The answer row and the
     * totals are saved in one transaction: both or neither.
     *
     * @param attemptId    the round
     * @param questionId   the question being answered
     * @param chosenOption the chosen letter, "A"–"D" (validated by the DTO)
     * @param email        email of the logged-in user
     * @return correct or not, the correct letter, and the explanation
     */
    @Transactional
    public PracticeAnswerResponse answer(Long attemptId, Long questionId,
                                         String chosenOption, String email) {

        Attempt attempt = findOwnRunningAttempt(attemptId, email);

        AttemptQuestion row = attemptQuestionRepository
                .findByAttemptIdAndQuestionId(attempt.getId(), questionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Question " + questionId + " is not part of this round."));

        if (row.isAnswered()) {
            throw new ConflictException("This question has already been answered.");
        }

        Question question = row.getQuestion();
        String correctOption = correctOptionOf(question);
        boolean correct = correctOption.equals(chosenOption);
        LocalDateTime now = LocalDateTime.now();

        row.recordAnswer(chosenOption, correct, now);
        attemptQuestionRepository.save(row);

        addToSubjectStats(attempt.getUser(), question.getSubject(), correct, now);

        return new PracticeAnswerResponse(correct, correctOption, question.getExplanation());
    }

    /**
     * Loads a round and checks that it is the user's own and still running.
     */
    private Attempt findOwnRunningAttempt(Long attemptId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Attempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new NotFoundException(
                        "Practice round not found with ID: " + attemptId));

        if (!attempt.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not own this practice round.");
        }
        if (attempt.isFinished()) {
            throw new ConflictException("This practice round has already been finished.");
        }
        return attempt;
    }

    /**
     * The correct option as a letter (A–D).
     * <p>
     * Questions still store the correct option's text. Until #155 changes that,
     * the letter is found by matching the text against the four options.
     * After #155 this becomes a single read of the stored letter.
     */
    private String correctOptionOf(Question question) {
        String correctText = question.getCorrectAnswer();
        if (correctText.equals(question.getOptionA())) return "A";
        if (correctText.equals(question.getOptionB())) return "B";
        if (correctText.equals(question.getOptionC())) return "C";
        if (correctText.equals(question.getOptionD())) return "D";
        throw new IllegalStateException(
                "Question " + question.getId() + " has a correct answer that matches no option.");
    }

    /**
     * Adds one answer to the student's totals for the subject.
     * <p>
     * The update adds 1 inside the database, so two answers at the same time
     * can't overwrite each other. If the student has no row for the subject yet,
     * one is created. (If two first answers for a subject arrive at exactly the
     * same moment, the unique constraint rejects the second insert with 409 and
     * nothing from that request is saved; the client can send it again.)
     */
    private void addToSubjectStats(User user, Subject subject, boolean correct, LocalDateTime answeredAt) {
        long correctIncrement = correct ? 1 : 0;
        int updated = subjectStatsRepository.addAnswer(user, subject, correctIncrement, answeredAt);

        if (updated == 0) {
            SubjectStats stats = new SubjectStats(user, subject);
            stats.setAnswered(1);
            stats.setCorrect(correctIncrement);
            stats.setLastAnsweredAt(answeredAt);
            subjectStatsRepository.save(stats);
        }
    }

    // Leaves out correctAnswer and explanation on purpose.
    private ExamQuestionResponse toQuestionResponse(Question question) {
        return new ExamQuestionResponse(
                question.getId(),
                question.getQuestionText(),
                question.getOptionA(),
                question.getOptionB(),
                question.getOptionC(),
                question.getOptionD(),
                question.getImageUrl()
        );
    }
}
