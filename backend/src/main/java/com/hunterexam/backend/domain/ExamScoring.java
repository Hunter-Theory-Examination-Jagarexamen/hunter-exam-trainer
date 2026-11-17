package com.hunterexam.backend.domain;

/**
 * Scoring rules for the mock exam — the one place the pass mark lives (#205).
 * <p>
 * Both the exam result page (via {@code ExamService.submitExam}) and the
 * admin's "Godkänd" / "Ej godkänd" (via {@code AdminService}) read from here,
 * so a change to the pass mark only needs to happen in this file.
 */
public final class ExamScoring {

    public static final int TOTAL_QUESTIONS = 70;
    public static final int PASS_CORRECT_ANSWERS = 60;

    private ExamScoring() {}

    /**
     * True only when the exam has the full {@value #TOTAL_QUESTIONS} questions
     * and at least {@value #PASS_CORRECT_ANSWERS} are correct. An old exam
     * with a different question count never counts as passed.
     */
    public static boolean isPassed(long correctAnswers, long totalQuestions) {
        return totalQuestions == TOTAL_QUESTIONS
            && correctAnswers >= PASS_CORRECT_ANSWERS;
    }
}
