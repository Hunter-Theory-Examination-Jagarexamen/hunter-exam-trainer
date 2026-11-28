package com.hunterexam.backend.repository.projection;

/**
 * A subject and how many questions it holds in the question bank.
 * <p>
 * The question count is the denominator of the per-subject mastery
 * percentage: the questions that exist now, whether or not the student has
 * answered them.
 *
 * @param subjectId     the subject
 * @param subjectName   its name
 * @param questionCount questions in the subject right now (0 for an empty subject)
 */
public record SubjectQuestionCount(Long subjectId, String subjectName, Long questionCount) {}
