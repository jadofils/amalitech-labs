package tests.concurrent;

import main.concurrent.AuditTrail;
import main.concurrent.AuditTrail.AuditEntry;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuditTrailTest {

    @Test
    @DisplayName("noOp() never creates a thread and getEntries() stays empty no matter how many record() calls are made")
    void noOpDoesNothingTest() throws Exception {
        AuditTrail auditTrail = AuditTrail.noOp();

        Future<?> future = auditTrail.append("ADD", "STUDENT", "STU001", "should be discarded");
        future.get(1, TimeUnit.SECONDS); // already completed - must not hang

        assertTrue(auditTrail.getEntries().isEmpty());
        assertDoesNotThrow(auditTrail::shutdown);
    }

    @Test
    @DisplayName("active() records an entry with the exact fields passed in, retrievable once the write completes")
    void activeRecordsEntryTest() throws InterruptedException, ExecutionException, TimeoutException {
        AuditTrail auditTrail = AuditTrail.active();
        try {
            auditTrail.append("ADD", "STUDENT", "STU001", "Added student Alice Johnson")
                    .get(2, TimeUnit.SECONDS);

            List<AuditEntry> entries = auditTrail.getEntries();
            assertEquals(1, entries.size());
            AuditEntry entry = entries.get(0);
            assertEquals("ADD", entry.action());
            assertEquals("STUDENT", entry.entityType());
            assertEquals("STU001", entry.entityId());
            assertEquals("Added student Alice Johnson", entry.details());
            assertNotNull(entry.timestamp());
        } finally {
            auditTrail.shutdown();
        }
    }

    @Test
    @DisplayName("Many concurrent record() calls never lose or corrupt an entry - every one lands intact")
    void concurrentRecordsAreSerializedTest() throws InterruptedException {
        AuditTrail auditTrail = AuditTrail.active();
        int callers = 8;
        int recordsPerCaller = 50;
        CountDownLatch done = new CountDownLatch(callers);

        try {
            for (int c = 0; c < callers; c++) {
                int callerId = c;
                new Thread(() -> {
                    for (int i = 0; i < recordsPerCaller; i++) {
                        auditTrail.append("ADD", "GRADE", "GRD" + callerId + "-" + i, "detail " + callerId + "-" + i);
                    }
                    done.countDown();
                }).start();
            }

            assertTrue(done.await(10, TimeUnit.SECONDS), "caller threads did not finish submitting in time");
            waitForPendingWrites(auditTrail);

            List<AuditEntry> entries = auditTrail.getEntries().stream()
                    .filter(e -> !"__drain__".equals(e.entityId()))
                    .toList();
            assertEquals(callers * recordsPerCaller, entries.size(), "every submitted record() must land exactly once");
            long distinctEntityIds = entries.stream().map(AuditEntry::entityId).distinct().count();
            assertEquals(entries.size(), distinctEntityIds, "no entry may be duplicated or overwrite another");
        } finally {
            auditTrail.shutdown();
        }
    }

    @Test
    @DisplayName("shutdown() completes cleanly and can be called more than once without throwing")
    void shutdownIsCleanAndIdempotentTest() {
        AuditTrail auditTrail = AuditTrail.active();
        auditTrail.append("ADD", "STUDENT", "STU001", "irrelevant");

        assertDoesNotThrow(auditTrail::shutdown);
        assertDoesNotThrow(auditTrail::shutdown);
    }

    @Test
    @DisplayName("shutdown() calls shutdownNow() when awaitTermination times out (mocked ExecutorService, no real 30s wait)")
    void shutdownCallsShutdownNowOnTimeoutTest() {
        ExecutorService mockExecutor = mock(ExecutorService.class);
        // awaitTermination is left unstubbed, so Mockito's default (false) drives the timeout branch.
        AuditTrail auditTrail = AuditTrail.withExecutor(mockExecutor);

        auditTrail.shutdown();

        verify(mockExecutor).shutdown();
        verify(mockExecutor).shutdownNow();
    }

    @Test
    @DisplayName("shutdown() handles being interrupted while awaiting termination, restoring the interrupt flag")
    void shutdownHandlesInterruptedAwaitTest() {
        AuditTrail auditTrail = AuditTrail.active();
        Thread.currentThread().interrupt();
        try {
            assertDoesNotThrow(auditTrail::shutdown);
            assertTrue(Thread.interrupted(), "interrupt flag should be restored after shutdown() catches InterruptedException");
        } finally {
            Thread.interrupted(); // clear, in case the assertion above failed first
        }
    }

    /** Drains the single-thread executor's queue by submitting one more record() and waiting for it. */
    private void waitForPendingWrites(AuditTrail auditTrail) throws InterruptedException {
        try {
            auditTrail.append("ADD", "STUDENT", "__drain__", "").get(5, TimeUnit.SECONDS);
        } catch (ExecutionException | TimeoutException e) {
            throw new AssertionError("pending audit writes did not drain in time", e);
        }
    }
}
