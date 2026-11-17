package com.hunterexam.backend.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The pass mark for the mock exam (#205). Both the result page and the
 * admin's "Godkänd" / "Ej godkänd" read from {@link ExamScoring}, so this
 * is the one place the rule is verified.
 */

class ExamScoringTest {

    @Test
    void sixtyOfSeventyPasses() {
        assertThat(ExamScoring.isPassed(60, 70)).isTrue();
    }

    @Test
    void fiftyNineOfSeventyFails() {
        assertThat(ExamScoring.isPassed(59, 70)).isFalse();
    }

    @Test
    void moreThanSixtyStillPasses() {
        assertThat(ExamScoring.isPassed(70, 70)).isTrue();
    }

    @Test
    void wrongTotalNeverPasses() {
        // An old attempt with fewer questions can't pass by accident. :)
        assertThat(ExamScoring.isPassed(60, 10)).isFalse();
    }
}
