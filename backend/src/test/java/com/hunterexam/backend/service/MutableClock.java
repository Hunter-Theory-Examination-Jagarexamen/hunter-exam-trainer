package com.hunterexam.backend.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * Test-only clock with an adjustable time source.
 * <p>
 * Rate-limit tests move it forward instead of sleeping, so they stay fast and
 * deterministic.
 */
final class MutableClock extends Clock {

    private Instant now = Instant.parse("2026-01-01T10:00:00Z");

    @Override
    public ZoneId getZone() {
        return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return this;
    }

    @Override
    public Instant instant() {
        return now;
    }

    void advance(Duration duration) {
        now = now.plus(duration);
    }

    void advanceSeconds(long seconds) {
        advance(Duration.ofSeconds(seconds));
    }
}
