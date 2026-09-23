package com.hunterexam.backend.entity;

import jakarta.persistence.*;
import jakarta.persistence.GeneratedValue;

import java.time.LocalDateTime;

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

}
