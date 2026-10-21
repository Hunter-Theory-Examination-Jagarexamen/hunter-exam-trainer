package com.hunterexam.backend.service;

import com.hunterexam.backend.exception.TooManyRequestsException;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure unit tests of the forgot-password limiter: one slot per normalized
 * address per window, bounded memory, atomic behaviour under concurrency.
 */
class PasswordResetRateLimiterTests {

    private static final long WINDOW_SECONDS = 60;
    private static final String EMAIL = "student@example.test";
    private static final String REJECTED_MESSAGE = "Too many password reset requests. Please try again later.";

    private final MutableClock clock = new MutableClock();

    private PasswordResetRateLimiter limiter(int maxTrackedEmails) {
        return new PasswordResetRateLimiter(WINDOW_SECONDS, maxTrackedEmails, clock);
    }

    @Test
    void firstRequestIsAcceptedAndASecondOneInsideTheWindowIsRejected() {
        PasswordResetRateLimiter limiter = limiter(100);

        assertDoesNotThrow(() -> limiter.check(EMAIL));

        TooManyRequestsException rejected =
                assertThrows(TooManyRequestsException.class, () -> limiter.check(EMAIL));
        assertEquals(REJECTED_MESSAGE, rejected.getMessage());
        assertEquals(1, limiter.trackedEmailCount());
    }

    @Test
    void addressBecomesEligibleAgainOnceTheWindowHasPassed() {
        PasswordResetRateLimiter limiter = limiter(100);
        limiter.check(EMAIL);

        clock.advanceSeconds(59);
        assertThrows(TooManyRequestsException.class, () -> limiter.check(EMAIL));

        // Exactly 60 seconds after the accepted request the slot is free again.
        clock.advanceSeconds(1);
        assertDoesNotThrow(() -> limiter.check(EMAIL));
        assertEquals(1, limiter.trackedEmailCount());
    }

    @Test
    void casingAndPaddingVariantsShareTheSameSlot() {
        PasswordResetRateLimiter limiter = limiter(100);
        limiter.check(EMAIL);

        for (String variant : new String[]{"Student@Example.TEST", "STUDENT@EXAMPLE.TEST",
                "  student@example.test  ", "\tstudent@example.test"}) {
            TooManyRequestsException rejected =
                    assertThrows(TooManyRequestsException.class, () -> limiter.check(variant), variant);
            assertEquals(REJECTED_MESSAGE, rejected.getMessage());
        }
        assertEquals(1, limiter.trackedEmailCount());
    }

    @Test
    void differentAddressesAreCountedSeparately() {
        PasswordResetRateLimiter limiter = limiter(100);

        limiter.check("first@example.test");
        assertDoesNotThrow(() -> limiter.check("second@example.test"));

        assertEquals(2, limiter.trackedEmailCount());
    }

    @Test
    void rememberedAddressesNeverExceedTheConfiguredCap() {
        PasswordResetRateLimiter limiter = limiter(3);

        for (int i = 0; i < 200; i++) {
            String address = "student" + i + "@example.test";
            assertDoesNotThrow(() -> limiter.check(address));
            assertTrue(limiter.trackedEmailCount() <= 3,
                    "tracked addresses must stay bounded, was " + limiter.trackedEmailCount());
        }
    }

    @Test
    void expiredAddressesAreForgottenOnTheNextCheck() {
        PasswordResetRateLimiter limiter = limiter(100);
        limiter.check(EMAIL);
        limiter.check("other@example.test");
        assertEquals(2, limiter.trackedEmailCount());

        clock.advanceSeconds(WINDOW_SECONDS + 1);

        limiter.check("fresh@example.test");
        assertEquals(1, limiter.trackedEmailCount());
    }

    @Test
    void concurrentChecksAcceptExactlyOneRequest() throws Exception {
        PasswordResetRateLimiter limiter = limiter(100);
        int threads = 8;
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger accepted = new AtomicInteger();
        AtomicInteger rejected = new AtomicInteger();

        try (var executor = Executors.newFixedThreadPool(threads)) {
            Callable<Boolean> task = () -> {
                assertTrue(start.await(10, TimeUnit.SECONDS));
                try {
                    limiter.check(EMAIL);
                    return true;
                } catch (TooManyRequestsException ex) {
                    return false;
                }
            };
            List<Future<Boolean>> futures = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                futures.add(executor.submit(task));
            }
            start.countDown();
            for (Future<Boolean> future : futures) {
                if (future.get(15, TimeUnit.SECONDS)) {
                    accepted.incrementAndGet();
                } else {
                    rejected.incrementAndGet();
                }
            }
        }

        assertEquals(1, accepted.get());
        assertEquals(threads - 1, rejected.get());
        assertEquals(1, limiter.trackedEmailCount());
    }
}
