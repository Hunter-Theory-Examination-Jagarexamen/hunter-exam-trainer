package com.hunterexam.backend.service;

import com.hunterexam.backend.dto.ExamQuestionResponse;
import com.hunterexam.backend.dto.PracticeAnswerResponse;
import com.hunterexam.backend.dto.PracticeFinishResponse;
import com.hunterexam.backend.dto.PracticeStartResponse;
import com.hunterexam.backend.entity.Attempt;
import com.hunterexam.backend.entity.AttemptQuestion;
import com.hunterexam.backend.entity.AttemptType;
import com.hunterexam.backend.entity.PracticeResult;
import com.hunterexam.backend.entity.Question;
import com.hunterexam.backend.entity.Subject;
import com.hunterexam.backend.entity.SubjectStats;
import com.hunterexam.backend.entity.User;
import com.hunterexam.backend.exception.ConflictException;
import com.hunterexam.backend.exception.NotFoundException;
import com.hunterexam.backend.repository.AttemptQuestionRepository;
import com.hunterexam.backend.repository.AttemptRepository;
import com.hunterexam.backend.repository.PracticeResultRepository;
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
import java.util.Set;

/**
 * Practice rounds stored per question (#168): start a round, answer one
 * question at a time (checked on the server), finish the round.
 */
@Service
public class PracticeAttemptService {

    // The round types this service handles. Mock exams are not included.
    private static final Set<AttemptType> PRACTICE_TYPES =
            Set.of(AttemptType.SUBJECT_PRACTICE, AttemptType.RANDOM_PRACTICE);

    private final AttemptRepository attemptRepository;
    private final AttemptQuestionRepository attemptQuestionRepository;
    private final PracticeResultRepository practiceResultRepository;
    private final QuestionRepository questionRepository;
    private final SubjectRepository subjectRepository;
    private final SubjectStatsRepository subjectStatsRepository;
    private final UserRepository userRepository;

    public PracticeAttemptService(
            AttemptRepository attemptRepository,
            AttemptQuestionRepository attemptQuestionRepository,
            PracticeResultRepository practiceResultRepository,
            QuestionRepository questionRepository,
            SubjectRepository subjectRepository,
            SubjectStatsRepository subjectStatsRepository,
            UserRepository userRepository
    ) {
        this.attemptRepository = attemptRepository;
        this.attemptQuestionRepository = attemptQuestionRepository;
        this.practiceResultRepository = practiceResultRepository;
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

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new NotFoundException("Subject not found with ID: " + subjectId));

        List<Question> questions = questionRepository.findBySubjectId(subjectId);
        if (questions.isEmpty()) {
            throw new ConflictException("This subject has no questions yet.");
        }

        // One open practice round per student: close any earlier round that
        // was left without finishing (tab closed, lost connection, ...).
        // No timer needed: starting a new round is what closes the old one.
        for (Attempt leftRound : attemptRepository.findByUserAndFinishedAtIsNullAndTypeIn(user, PRACTICE_TYPES)) {
            close(leftRound);
        }

        Attempt attempt = attemptRepository.save(
                new Attempt(user, AttemptType.SUBJECT_PRACTICE, LocalDateTime.now()));

        List<AttemptQuestion> rows = new ArrayList<>();
        for (int position = 0; position < questions.size(); position++) {
            rows.add(new AttemptQuestion(attempt, questions.get(position), position));
        }
        attemptQuestionRepository.saveAll(rows);

        ensureSubjectStatsExist(user, Set.of(subject));

        List<ExamQuestionResponse> questionDtos = questions.stream()
                .map(this::toQuestionResponse)
                .toList();

        return new PracticeStartResponse(attempt.getId(), subject.getName(), questionDtos);
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
     * Finishes a practice round and returns its totals, counted from the saved answers.
     * <p>
     * {@code totalQuestions} is the number of questions in the round and
     * {@code answeredQuestions} how many of them were answered, so the page can
     * show e.g. "3 of 7 answered, 2 correct".
     *
     * @param attemptId the round
     * @param email     email of the logged-in user
     * @return correct, answered and total questions
     */
    @Transactional
    public PracticeFinishResponse finish(Long attemptId, String email) {

        Attempt attempt = findOwnRunningAttempt(attemptId, email);
        return close(attempt);
    }

    /**
     * Closes a round: sets {@code finishedAt}, counts the totals from the saved
     * answers and saves the bridge result. Used by {@link #finish} and by
     * {@link #start} for rounds that were left without finishing.
     */
    private PracticeFinishResponse close(Attempt attempt) {
        attempt.setFinishedAt(LocalDateTime.now());
        attemptRepository.save(attempt);

        List<AttemptQuestion> rows = attemptQuestionRepository.findByAttemptIdOrderByPosition(attempt.getId());
        long total = rows.size();
        long answered = rows.stream().filter(AttemptQuestion::isAnswered).count();
        long correct = rows.stream().filter(row -> Boolean.TRUE.equals(row.getCorrect())).count();

        if (answered > 0) {
            savePracticeResultBridge(attempt, rows.get(0).getQuestion().getSubject(), correct, answered);
        }

        return new PracticeFinishResponse(correct, answered, total);
    }

    /**
     * Temporary: keeps the old statistics working until they read from
     * SubjectStats (issue 4 of the refactor plan), then remove this and
     * PracticeResult (issue 5).
     * <p>
     * Saves the same one-row summary the old practice page sent itself, but
     * with totals counted by the server. A subject practice round has one
     * subject, so the first question's subject is the round's subject.
     * Rounds with no answers are skipped, so they don't show up as 0 %.
     * <p>
     * Only answered questions count (2026-10-29): a round left after 3 of 7
     * questions, 2 right, is saved as 2 of 3, not 2 of 7. Practice is for
     * learning; "skipped counts as wrong" belongs to the mock exam.
     */
    private void savePracticeResultBridge(Attempt attempt, Subject subject, long correct, long answered) {
        PracticeResult result = new PracticeResult();
        result.setUser(attempt.getUser());
        result.setSubject(subject);
        result.setCorrectAnswers(correct);
        result.setTotalQuestions(answered);
        result.setScore(Math.round((double) correct / answered * 100));
        result.setCompletedAt(attempt.getFinishedAt());
        practiceResultRepository.save(result);
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
     * As of #155, {@code Question.correctAnswer} stores the letter directly.
     */
    private String correctOptionOf(Question question) {
        return question.getCorrectAnswer();
    }


    /**
     * Creates the student's totals row (0 answered, 0 correct) for each subject
     * in the round that doesn't have one yet.
     * <p>
     * Done at start so that {@link #answer} only ever updates and never inserts:
     * an insert there could clash with another answer arriving at the same moment.
     * Two starts at the same moment (e.g. a double click) can still clash here;
     * the unique constraint rejects the second, and
     * {@code PracticeAttemptController.start} retries it once.
     */
    private void ensureSubjectStatsExist(User user, Set<Subject> subjects) {
        for (Subject subject : subjects) {
            if (!subjectStatsRepository.existsByUserAndSubject(user, subject)) {
                subjectStatsRepository.save(new SubjectStats(user, subject));
            }
        }
    }

    /**
     * Adds one answer to the student's totals for the subject.
     * <p>
     * The update adds 1 inside the database, so two answers at the same time
     * can't overwrite each other. The row always exists, because
     * {@link #start} creates it.
     */
    private void addToSubjectStats(User user, Subject subject, boolean correct, LocalDateTime answeredAt) {
        long correctIncrement = correct ? 1 : 0;
        int updated = subjectStatsRepository.addAnswer(user, subject, correctIncrement, answeredAt);

        // 0 rows updated means start didn't create the row: a bug. Fail loudly
        // instead of inserting here, which could clash with a parallel answer.
        if (updated == 0) {
            throw new IllegalStateException(
                    "No totals row for subject " + subject.getId() + "; it should have been created at start.");
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
