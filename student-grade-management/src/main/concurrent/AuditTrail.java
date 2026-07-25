package main.concurrent;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * A durable, structured record of data-changing actions (US-9/PBI-9) - distinct from
 * {@link main.logging.Logger}, which is general debug/diagnostic output, not a record meant to
 * answer "who changed what". Every {@link #record} call is submitted to a single-thread executor,
 * so concurrent callers can never interleave or corrupt one another's entry: only one write is
 * ever in flight, and every entry is written whole or not at all.
 *
 * <p>{@link #active()} builds a real, single-thread-executor-backed instance; {@link #noOp()}
 * builds one that does nothing and never creates a thread at all - the default for
 * {@link main.manager.StudentManager}/{@link main.manager.GradeManager}'s existing two-argument
 * constructors, so every one of their pre-PBI-9 callers (essentially this whole test suite) keeps
 * compiling and passing completely unmodified. {@link main.app.Main} wires a real {@link #active()}
 * instance for the actual running application.
 */
public final class AuditTrail {

    private final ExecutorService executor;
    private final List<AuditEntry> entries;

    private AuditTrail(ExecutorService executor, List<AuditEntry> entries) {
        this.executor = executor;
        this.entries = entries;
    }

    public static AuditTrail active() {
        ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "audit-trail-writer");
            thread.setDaemon(true);
            return thread;
        });
        return new AuditTrail(executor, new CopyOnWriteArrayList<>());
    }

    /** Does nothing, ever - no thread or executor is created. */
    public static AuditTrail noOp() {
        return new AuditTrail(null, List.of());
    }

    /**
     * PBI-10: lets a test substitute a mocked {@link ExecutorService}, to verify {@link #shutdown()}'s
     * timeout/interrupt handling deterministically instead of waiting on a real 30-second timeout.
     */
    public static AuditTrail withExecutor(ExecutorService executor) {
        return new AuditTrail(executor, new CopyOnWriteArrayList<>());
    }

    /**
     * Appends one entry asynchronously - the returned {@link Future} is there for callers (mainly
     * tests) that need to wait for it to actually be written; ordinary business-code call sites are
     * expected to ignore it and move on, exactly like {@link main.logging.Logger} calls do.
     */
    public Future<Boolean> append(String action, String entityType, String entityId, String details) {
        if (executor == null) {
            return CompletableFuture.completedFuture(null);
        }
        AuditEntry entry = new AuditEntry(action, entityType, entityId, details, Instant.now());
        return executor.submit(() -> entries.add(entry));
    }

    /** A point-in-time snapshot; safe to read from any thread while writes continue on the executor. */
    public List<AuditEntry> getEntries() {
        return List.copyOf(entries);
    }

    /** No-op for a {@link #noOp()} instance. Cancels no in-flight write; waits for the executor to drain first. */
    public void shutdown() {
        if (executor == null) {
            return;
        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /** One audit-trail entry: what changed, on what kind of entity, with which ID, when. */
    public record AuditEntry(String action, String entityType, String entityId, String details, Instant timestamp) {
    }
}
