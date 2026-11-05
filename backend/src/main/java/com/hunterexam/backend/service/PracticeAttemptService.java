package com.hunterexam.backend.service;

import com.hunterexam.backend.dto.QuestionResponse;
import com.hunterexam.backend.dto.PracticeAnswerResponse;
import com.hunterexam.backend.dto.PracticeFinishResponse;
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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Practice rounds stored per question (#168): start a round, answer one
 * question at a time (checked on the server), finish the round.
 * <p>
 * A round covers one subject ({@link #start}) or, for random practice (#151),
 * up to {@value #RANDOM_ROUND_SIZE} questions picked by the server from the
 * whole bank ({@link #startRandom}). Both kinds share the same lifecycle and
 * the same rules: one open round per student, answers saved as they are given,
 * totals per subject of each answered question.
 */
@Service
public class PracticeAttemptService {

    // The round types this service handles. Mock exams are not included.
    private static final Set<AttemptType> PRACTICE_TYPES =
            Set.of(AttemptType.SUBJECT_PRACTICE, AttemptType.RANDOM_PRACTICE);

    /**
     * How many questions a random round has when the bank has that many:
     * enough for a real round, few enough to finish in one sitting. A smaller
     * bank gives a shorter round.
     */
    private static final int RANDOM_ROUND_SIZE = 20;

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

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new NotFoundException("Subject not found with ID: " + subjectId));

        List<Question> questions = questionRepository.findBySubjectId(subjectId);
        if (questions.isEmpty()) {
            throw new ConflictException("This subject has no questions yet.");
        }

        return openRound(user, AttemptType.SUBJECT_PRACTICE, questions, subject.getName());
    }

    /**
     * Starts a random practice round: questions from all subjects (#151).
     * <p>
     * The server picks the questions ({@link #selectRandomQuestions}), so the
     * client never chooses them and never sees the correct answers. An empty
     * bank is a conflict, the same as a subject with no questions.
     *
     * @param email email of the logged-in user
     * @return the round's id and its questions; {@code subjectName} is null,
     * because a random round mixes subjects
     */
    @Transactional
    public PracticeStartResponse startRandom(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        List<Question> questions = selectRandomQuestions();
        if (questions.isEmpty()) {
            throw new ConflictException("There are no questions to practise yet.");
        }

        return openRound(user, AttemptType.RANDOM_PRACTICE, questions, null);
    }

    /**
     * Opens a round: closes any earlier practice round the student left
     * running, saves the new round with one {@link AttemptQuestion} row per
     * question (in the order they are given), makes sure the student has a
     * totals row for every subject in the round, and returns the questions
     * without their correct answers.
     * <p>
     * The caller has already checked that there is at least one question, so
     * a round is never opened (and no earlier round closed) for an empty bank.
     *
     * @param user        the student
     * @param type        SUBJECT_PRACTICE or RANDOM_PRACTICE
     * @param questions   the round's questions, in the order to show them
     * @param subjectName the subject's name, or null for a mixed round
     * @return the round's id and its questions
     */
    private PracticeStartResponse openRound(User user, AttemptType type,
                                            List<Question> questions, String subjectName) {

        // One open practice round per student: close any earlier round that
        // was left without finishing (tab closed, lost connection, ...).
        // No timer needed: starting a new round is what closes the old one.
        for (Attempt leftRound : attemptRepository.findByUserAndFinishedAtIsNullAndTypeIn(user, PRACTICE_TYPES)) {
            close(leftRound);
        }

        Attempt attempt = attemptRepository.save(new Attempt(user, type, LocalDateTime.now()));

        List<AttemptQuestion> rows = new ArrayList<>();
        for (int position = 0; position < questions.size(); position++) {
            rows.add(new AttemptQuestion(attempt, questions.get(position), position));
        }
        attemptQuestionRepository.saveAll(rows);

        ensureSubjectStatsExist(user, questions.stream()
                .map(Question::getSubject)
                .collect(Collectors.toSet()));

        List<QuestionResponse> questionDtos = questions.stream()
                .map(this::toQuestionResponse)
                .toList();

        return new PracticeStartResponse(attempt.getId(), subjectName, questionDtos);
    }

    /**
     * Picks up to {@value #RANDOM_ROUND_SIZE} questions at random from the
     * whole bank, without duplicates, in random order.
     * <p>
     * The questions of each subject are shuffled, and the subjects are then
     * walked round-robin, so a subject with many questions can't fill the
     * round alone and every subject with a question is represented while
     * there are still slots left. The picked questions are shuffled once
     * more, so the student sees them in a random order.
     *
     * @return the chosen questions, or an empty list when the bank is empty
     */
    private List<Question> selectRandomQuestions() {

        List<Question> bank = questionRepository.findAllWithSubject();
        if (bank.isEmpty()) {
            return List.of();
        }

        Map<Long, List<Question>> bySubject = new LinkedHashMap<>();
        for (Question question : bank) {
            bySubject.computeIfAbsent(question.getSubject().getId(), id -> new ArrayList<>())
                    .add(question);
        }
        bySubject.values().forEach(Collections::shuffle);

        List<Question> chosen = new ArrayList<>();
        for (int position = 0; chosen.size() < RANDOM_ROUND_SIZE; position++) {
            boolean pickedAny = false;
            for (List<Question> subjectQuestions : bySubject.values()) {
                if (position >= subjectQuestions.size()) {
                    continue;
                }
                chosen.add(subjectQuestions.get(position));
                pickedAny = true;
                if (chosen.size() == RANDOM_ROUND_SIZE) {
                    break;
                }
            }
            if (!pickedAny) {
                break; // every subject has been exhausted
            }
        }

        Collections.shuffle(chosen);
        return chosen;
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
     * Closes a round: sets {@code finishedAt} and counts the totals from the
     * saved answers. Used by {@link #finish} and by
     * {@link #start} for rounds that were left without finishing.
     */
    private PracticeFinishResponse close(Attempt attempt) {
        attempt.setFinishedAt(LocalDateTime.now());
        attemptRepository.save(attempt);

        List<AttemptQuestion> rows = attemptQuestionRepository.findByAttemptIdOrderByPosition(attempt.getId());
        long total = rows.size();
        long answered = rows.stream().filter(AttemptQuestion::isAnswered).count();
        long correct = rows.stream().filter(row -> Boolean.TRUE.equals(row.getCorrect())).count();

        return new PracticeFinishResponse(correct, answered, total);
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
    private QuestionResponse toQuestionResponse(Question question) {
        return new QuestionResponse(
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
