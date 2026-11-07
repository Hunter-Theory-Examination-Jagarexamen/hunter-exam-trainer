package com.hunterexam.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

/**
 * A message from a student to the course leaders: a problem with a question,
 * or general feedback about the app.
 * <p>
 * The sender is stored (for the daily limit) but never shown to admins.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Feedback {

    /** Longest allowed text, also used by the request validation. */
    public static final int MAX_TEXT_LENGTH = 2000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FeedbackType type;

    @Column(nullable = false, length = MAX_TEXT_LENGTH)
    private String text;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private boolean handled = false;

    /** Who sent it. Empty if the account is deleted later; the feedback stays. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private User sender;

    /** The reported question, if any. Empty if the question is deleted later. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Question question;

    /** The question's text when it was reported, so later edits don't change the report. */
    @Column(length = 1000)
    private String questionTextSnapshot;

    public Feedback(User sender, FeedbackType type, String text, Question question,
                    LocalDateTime createdAt) {
        this.sender = sender;
        this.type = type;
        this.text = text;
        this.question = question;
        this.questionTextSnapshot = question == null ? null : question.getQuestionText();
        this.createdAt = createdAt;
    }
}
