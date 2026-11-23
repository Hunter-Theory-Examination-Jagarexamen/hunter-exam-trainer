package com.hunterexam.backend.repository.projection;

/**
 * How many questions a subject's part of the question bank holds.
 * <p>
 * The denominator of the per-subject mastery percentage: the questions that
 * exist now, whether or not the student has answered them.
 *
 * @param subjectId the subject
 * @param total     questions in the subject right now (0 for an empty subject)
 */
public record SubjectQuestionCount(Long subjectId, Long total) {}
