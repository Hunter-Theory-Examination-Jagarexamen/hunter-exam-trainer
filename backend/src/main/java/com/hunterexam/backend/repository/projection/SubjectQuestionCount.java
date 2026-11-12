package com.hunterexam.backend.repository.projection;

/**
 * How many questions a subject has in the question bank: the "out of" number
 * for mastery per subject.
 *
 * @param subjectId     the subject
 * @param subjectName   its name
 * @param questionCount questions in the subject (0 if it has none)
 */
public record SubjectQuestionCount(Long subjectId, String subjectName, Long questionCount) {
}
