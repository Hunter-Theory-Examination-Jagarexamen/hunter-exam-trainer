package com.hunterexam.backend.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import jakarta.persistence.MapKeyColumn;
import java.util.Map;

@Entity
@Table(name = "exam_sessions")
public class ExamSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    private LocalDateTime startedAt;

    private boolean completed;

    @ElementCollection
    @CollectionTable(
        name = "exam_session_questions",
        joinColumns = @JoinColumn(name = "session_id")
    )
    @Column(name = "question_id")
    private List<Long> questionIds = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
        name = "exam_session_answers",
        joinColumns = @JoinColumn(name = "session_id")
    )
    @MapKeyColumn(name = "question_id")
    @Column(name = "answer")
    private Map<Long, String> answers = new HashMap<>();

    public ExamSession() {
    }

    public ExamSession(User user, LocalDateTime startedAt) {
        this.user = user;
        this.startedAt = startedAt;
        this.completed = false;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public List<Long> getQuestionIds() {
        return questionIds;
    }

    public void setQuestionIds(List<Long> questionIds) {
        this.questionIds = questionIds;
    }

    public Map<Long, String> getAnswers() {
        return answers;
    }

    public void setAnswers(Map<Long, String> answers) {
        this.answers = answers;
    }

    public void setAnswer(Long questionId, String answer) {
        this.answers.put(questionId, answer);
    }
}
