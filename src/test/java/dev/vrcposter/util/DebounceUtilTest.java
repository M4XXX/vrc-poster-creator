package dev.vrcposter.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DebounceUtilTest {

    private static final long DELAY_MS = 50;
    private static final long WAIT_MS  = 300;

    private ScheduledExecutorService executor;
    private DebounceUtil debouncer;

    @BeforeEach
    void setUp() {
        executor = Executors.newScheduledThreadPool(1);
        debouncer = new DebounceUtil(executor, DELAY_MS);
    }

    @AfterEach
    void tearDown() {
        debouncer.cancelAll();
        executor.shutdownNow();
    }

    @Test
    void singleCallFiresAfterDelay() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        debouncer.schedule("key", latch::countDown);
        assertTrue(latch.await(WAIT_MS, TimeUnit.MILLISECONDS),
            "Action should fire within the wait window");
    }

    @Test
    void rapidCallsWithSameKeyFireOnlyOnce() throws InterruptedException {
        AtomicInteger callCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(1);

        for (int i = 0; i < 10; i++) {
            debouncer.schedule("key", () -> {
                callCount.incrementAndGet();
                latch.countDown();
            });
            Thread.sleep(10); // calls spaced 10ms apart — all within 50ms window
        }

        assertTrue(latch.await(WAIT_MS, TimeUnit.MILLISECONDS),
            "Action should fire at least once");
        // Give extra time to ensure no second call arrives
        Thread.sleep(DELAY_MS * 3);
        assertEquals(1, callCount.get(),
            "Action must fire exactly once despite rapid scheduling");
    }

    @Test
    void lastCallValueIsUsed() throws InterruptedException {
        AtomicReference<String> lastValue = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        debouncer.schedule("key", () -> lastValue.set("first"));
        Thread.sleep(10);
        debouncer.schedule("key", () -> {
            lastValue.set("last");
            latch.countDown();
        });

        assertTrue(latch.await(WAIT_MS, TimeUnit.MILLISECONDS));
        assertEquals("last", lastValue.get(),
            "The last scheduled action should be the one that fires");
    }

    @Test
    void differentKeysFireIndependently() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);
        AtomicInteger countA = new AtomicInteger(0);
        AtomicInteger countB = new AtomicInteger(0);

        debouncer.schedule("keyA", () -> { countA.incrementAndGet(); latch.countDown(); });
        debouncer.schedule("keyB", () -> { countB.incrementAndGet(); latch.countDown(); });

        assertTrue(latch.await(WAIT_MS, TimeUnit.MILLISECONDS),
            "Both actions should fire");
        assertEquals(1, countA.get(), "keyA action should fire exactly once");
        assertEquals(1, countB.get(), "keyB action should fire exactly once");
    }

    @Test
    void cancelAllPreventsPendingActions() throws InterruptedException {
        AtomicInteger callCount = new AtomicInteger(0);

        debouncer.schedule("key", callCount::incrementAndGet);
        debouncer.cancelAll();

        // Wait past when the action would have fired
        Thread.sleep(DELAY_MS * 3);
        assertEquals(0, callCount.get(),
            "cancelAll should prevent the pending action from firing");
    }
}
