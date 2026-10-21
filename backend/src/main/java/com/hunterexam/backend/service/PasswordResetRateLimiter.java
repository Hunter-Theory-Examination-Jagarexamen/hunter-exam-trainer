package com.hunterexam.backend.service;

import com.hunterexam.backend.exception.TooManyRequestsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Allows at most one password reset request per email address per time window.
 * <p>
 * The counter lives in memory only, so no database table, cache or extra
 * dependency is needed. The limit is therefore <b>per application instance</b>
 * and is cleared on restart; several instances behind a load balancer count
 * independently. Expired entries are dropped on every check and at most
 * {@code maxTrackedEmails} addresses are remembered at once (the oldest entry
 * is dropped first), so memory stays bounded even if a client requests resets
 * for many different addresses.
 * <p>
 * Addresses are trimmed and lower-cased before comparison, which stops casing
 * or padding variants from opening a second slot. Neither addresses nor reset
 * tokens are ever written to the log.
 */
@Component
public class PasswordResetRateLimiter {

    /** Same text for registered and unregistered addresses, so the response never reveals account existence. */
    private static final String REJECTED_MESSAGE =
            "Too many password reset requests. Please try again later.";

    private static final Logger log = LoggerFactory.getLogger(PasswordResetRateLimiter.class);

    private final Duration window;
    private final int maxTrackedEmails;
    private final Clock clock;
    // Last accepted request per normalized email. Only touched while holding this instance's monitor.
    private final Map<String, Instant> lastRequestByNormalizedEmail = new HashMap<>();

    @Autowired
    public PasswordResetRateLimiter(
            @Value("${app.password-reset.rate-limit-window-seconds:60}") long windowSeconds,
            @Value("${app.password-reset.rate-limit-max-tracked-emails:10000}") int maxTrackedEmails) {
        this(windowSeconds, maxTrackedEmails, Clock.systemUTC());
    }

    /**
     * Creates a limiter with an injectable clock. Production wiring uses the constructor
     * above; tests use this one to move time forward without sleeping.
     *
     * @param windowSeconds    length of the limit window, at least one second
     * @param maxTrackedEmails upper bound on remembered addresses, at least one
     * @param clock            time source used for the window
     */
    public PasswordResetRateLimiter(long windowSeconds, int maxTrackedEmails, Clock clock) {
        this.window = Duration.ofSeconds(Math.max(1, windowSeconds));
        this.maxTrackedEmails = Math.max(1, maxTrackedEmails);
        this.clock = clock;
    }

    /**
     * Books one request for the address, or refuses it when the address already
     * used its slot inside the current window.
     * <p>
     * The check runs as a single atomic step, so concurrent requests for the same
     * address cannot both pass and trigger duplicate emails.
     *
     * @param email address as submitted by the client
     * @throws TooManyRequestsException when the address is still inside its window
     */
    public void check(String email) {
        String key = normalize(email);
        synchronized (this) {
            Instant now = clock.instant();
            Instant previous = lastRequestByNormalizedEmail.get(key);
            if (previous != null && previous.plus(window).isAfter(now)) {
                // No address and no token here: logs must stay free of both.
                log.debug("Forgot-password request rejected: address is still inside the rate-limit window");
                throw new TooManyRequestsException(REJECTED_MESSAGE);
            }
            discardExpired(now);
            makeRoom();
            lastRequestByNormalizedEmail.put(key, now);
        }
    }

    /**
     * @return how many addresses are currently remembered; used by tests and monitoring
     */
    int trackedEmailCount() {
        synchronized (this) {
            return lastRequestByNormalizedEmail.size();
        }
    }

    static String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private void discardExpired(Instant now) {
        lastRequestByNormalizedEmail.values()
                .removeIf(last -> !last.plus(window).isAfter(now));
    }

    private void makeRoom() {
        while (lastRequestByNormalizedEmail.size() >= maxTrackedEmails) {
            String oldestKey = null;
            Instant oldest = null;
            for (Map.Entry<String, Instant> entry : lastRequestByNormalizedEmail.entrySet()) {
                if (oldest == null || entry.getValue().isBefore(oldest)) {
                    oldestKey = entry.getKey();
                    oldest = entry.getValue();
                }
            }
            lastRequestByNormalizedEmail.remove(oldestKey);
        }
    }
}
