package com.hunterexam.backend.service;

import com.hunterexam.backend.dto.FeedbackRequest;
import com.hunterexam.backend.entity.Feedback;
import com.hunterexam.backend.entity.FeedbackType;
import com.hunterexam.backend.entity.Question;
import com.hunterexam.backend.entity.User;
import com.hunterexam.backend.exception.NotFoundException;
import com.hunterexam.backend.exception.TooManyRequestsException;
import com.hunterexam.backend.repository.FeedbackRepository;
import com.hunterexam.backend.repository.QuestionRepository;
import com.hunterexam.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/** Students' feedback and question reports to the course leaders. */
@Service
public class FeedbackService {

    /** Most feedback items one user may send in 24 hours. */
    static final int DAILY_LIMIT = 20;

    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;

    public FeedbackService(
            FeedbackRepository feedbackRepository,
            UserRepository userRepository,
            QuestionRepository questionRepository
    ) {
        this.feedbackRepository = feedbackRepository;
        this.userRepository = userRepository;
        this.questionRepository = questionRepository;
    }

    /**
     * Saves one feedback item from the logged-in user.
     *
     * @param email   the sender's email (from the login token)
     * @param request type, text and, for a question report, the question's id
     * @throws TooManyRequestsException if the user has sent {@value #DAILY_LIMIT} in the last 24 hours
     * @throws NotFoundException        if the question doesn't exist
     * @throws IllegalArgumentException if a question id comes with another type than QUESTION
     */
    @Transactional
    public void send(String email, FeedbackRequest request) {
        User sender = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        LocalDateTime now = LocalDateTime.now();
        if (feedbackRepository.countBySenderAndCreatedAtAfter(sender, now.minusHours(24)) >= DAILY_LIMIT) {
            throw new TooManyRequestsException("Too much feedback sent today. Please try again tomorrow.");
        }

        Question question = null;
        if (request.getQuestionId() != null) {
            if (request.getType() != FeedbackType.QUESTION) {
                throw new IllegalArgumentException("A question can only be attached to a question report");
            }
            question = questionRepository.findById(request.getQuestionId())
                    .orElseThrow(() -> new NotFoundException("Question not found"));
        }

        feedbackRepository.save(new Feedback(
                sender, request.getType(), request.getText().strip(), question, now));
    }
}
