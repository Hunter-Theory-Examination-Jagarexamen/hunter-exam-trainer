package com.hunterexam.backend.controller;

import com.hunterexam.backend.dto.PracticeAnswerRequest;
import com.hunterexam.backend.dto.PracticeAnswerResponse;
import com.hunterexam.backend.dto.PracticeFinishResponse;
import com.hunterexam.backend.dto.PracticeStartRequest;
import com.hunterexam.backend.dto.PracticeStartResponse;
import com.hunterexam.backend.service.PracticeAttemptService;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for practice rounds stored per question (#168).
 * <p>
 * The server checks each answer, so the client never gets the correct
 * answers in advance and never sends its own score.
 */
@RestController
@RequestMapping("/api/practice/attempts")
public class PracticeAttemptController {

    private final PracticeAttemptService practiceAttemptService;

    public PracticeAttemptController(PracticeAttemptService practiceAttemptService) {
        this.practiceAttemptService = practiceAttemptService;
    }

    /**
     * Starts a practice round with all questions of one subject.
     *
     * @param authentication authentication info from Spring Security
     * @param request        the subject to practise
     * @return the round's id and its questions, without correct answers
     */
    @PostMapping
    public PracticeStartResponse start(
            Authentication authentication,
            @Valid @RequestBody PracticeStartRequest request
    ) {
        try {
            return practiceAttemptService.start(request.getSubjectId(), authentication.getName());
        } catch (DataIntegrityViolationException e) {
            // Two starts at the same moment (double click, two tabs, React StrictMode
            // in development) can both try to create the student's SubjectStats row.
            // The losing start's transaction is rolled back, so it is retried once here,
            // outside it, in a new transaction; this time the row exists.
            return practiceAttemptService.start(request.getSubjectId(), authentication.getName());
        }
    }

    /**
     * Checks and saves the answer to one question in the round.
     *
     * @param authentication authentication info from Spring Security
     * @param attemptId      the round
     * @param request        the question and the chosen option (A–D)
     * @return correct or not, the correct option, and the explanation
     */
    @PostMapping("/{attemptId}/answers")
    public PracticeAnswerResponse answer(
            Authentication authentication,
            @PathVariable Long attemptId,
            @Valid @RequestBody PracticeAnswerRequest request
    ) {
        return practiceAttemptService.answer(
                attemptId,
                request.getQuestionId(),
                request.getChosenOption(),
                authentication.getName()
        );
    }

    /**
     * Finishes the round and returns its totals, counted by the server.
     *
     * @param authentication authentication info from Spring Security
     * @param attemptId      the round
     * @return correct answers and total questions
     */
    @PostMapping("/{attemptId}/finish")
    public PracticeFinishResponse finish(
            Authentication authentication,
            @PathVariable Long attemptId
    ) {
        return practiceAttemptService.finish(attemptId, authentication.getName());
    }
}
