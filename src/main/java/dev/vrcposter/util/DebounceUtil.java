package dev.vrcposter.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DebounceUtil {
    private static final Logger log = LoggerFactory.getLogger(DebounceUtil.class);

    private final ScheduledExecutorService executor;
    private final long delayMs;
    private final Map<String, Future<?>> pending = new HashMap<>();

    public DebounceUtil(ScheduledExecutorService executor, long delayMs) {
        this.executor = executor;
        this.delayMs = delayMs;
    }

    /**
     * Schedules action to run after the specified delayMs of inactivity on the given key.
     * Repeated calls with the same key within the window reset the timer.
     */
    public synchronized void schedule(String key, long delayMs, Runnable action) {
        Future<?> existing = pending.remove(key);
        if (existing != null) {
            existing.cancel(false);
        }
        log.debug("Debounce scheduled: key={}, delayMs={}", key, delayMs);
        pending.put(key, executor.schedule(() -> {
            synchronized (DebounceUtil.this) { pending.remove(key); }
            action.run();
        }, delayMs, TimeUnit.MILLISECONDS));
    }

    /** Schedules with the instance's configured delay. */
    public synchronized void schedule(String key, Runnable action) {
        schedule(key, delayMs, action);
    }

    /** Cancels all pending debounced actions. Call during application shutdown. */
    public synchronized void cancelAll() {
        pending.forEach((k, f) -> {
            if (f != null) {
                f.cancel(false);
            }
        });
        pending.clear();
        log.debug("All debounced actions cancelled");
    }
}
