package tests.concurrent;

import main.concurrent.LruCache;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class LruCacheTest {

    @Test
    @DisplayName("A non-positive capacity is rejected")
    void rejectsNonPositiveCapacityTest() {
        assertThrows(IllegalArgumentException.class, () -> new LruCache<String, String>(0));
        assertThrows(IllegalArgumentException.class, () -> new LruCache<String, String>(-1));
    }

    @Test
    @DisplayName("put() then get() returns the same value; a missing key returns null")
    void putThenGetRoundTripsTest() {
        LruCache<String, String> cache = new LruCache<>(10);
        cache.put("a", "apple");

        assertEquals("apple", cache.get("a"));
        assertNull(cache.get("nope"));
    }

    @Test
    @DisplayName("hitRate() is 0.0 with no reads, rises on a hit, and falls on a miss")
    void hitRateReflectsHitsAndMissesTest() {
        LruCache<String, String> cache = new LruCache<>(10);
        assertEquals(0.0, cache.hitRate(), 0.0001);

        cache.get("missing"); // miss
        assertEquals(0.0, cache.hitRate(), 0.0001);

        cache.put("a", "apple");
        cache.get("a"); // hit
        assertEquals(0.5, cache.hitRate(), 0.0001); // 1 hit / (1 hit + 1 miss)
    }

    @Test
    @DisplayName("invalidate() removes a key so the next get() is a miss")
    void invalidateRemovesEntryTest() {
        LruCache<String, String> cache = new LruCache<>(10);
        cache.put("a", "apple");
        cache.invalidate("a");

        assertNull(cache.get("a"));
        assertEquals(0, cache.size());
    }

    @Test
    @DisplayName("invalidate() on a missing key is a harmless no-op")
    void invalidateMissingKeyIsNoOpTest() {
        LruCache<String, String> cache = new LruCache<>(10);
        assertDoesNotThrow(() -> cache.invalidate("never-added"));
    }

    @Test
    @DisplayName("A null key is handled gracefully (get()/put()/invalidate() never throw, since ConcurrentHashMap itself can't store a null key)")
    void nullKeyIsHandledGracefullyTest() {
        LruCache<String, String> cache = new LruCache<>(10);

        assertNull(cache.get(null));
        assertDoesNotThrow(() -> cache.put(null, "value"));
        assertDoesNotThrow(() -> cache.invalidate(null));
        assertEquals(0, cache.size(), "a null-keyed put() must not actually be stored");
    }

    @Test
    @DisplayName("clear() empties the cache entirely")
    void clearEmptiesCacheTest() {
        LruCache<String, String> cache = new LruCache<>(10);
        cache.put("a", "apple");
        cache.put("b", "banana");

        cache.clear();

        assertEquals(0, cache.size());
        assertNull(cache.get("a"));
    }

    @Test
    @DisplayName("Adding beyond capacity evicts the least-recently-used entry, never one that was just accessed")
    void evictsLeastRecentlyUsedTest() {
        LruCache<String, String> cache = new LruCache<>(3);
        cache.put("a", "apple");
        cache.put("b", "banana");
        cache.put("c", "cherry");

        cache.get("a"); // "a" is now the most recently used; "b" becomes the least recently used
        cache.put("d", "date"); // must evict "b", not "a" or "c"

        assertEquals(3, cache.size());
        assertNotNull(cache.get("a"), "recently-accessed \"a\" must survive eviction");
        assertNull(cache.get("b"), "least-recently-used \"b\" must be the one evicted");
        assertNotNull(cache.get("c"));
        assertNotNull(cache.get("d"));
    }

    @Test
    @DisplayName("size() never exceeds capacity even under many concurrent puts from multiple threads")
    void concurrentPutsNeverExceedCapacityTest() throws InterruptedException {
        int capacity = 20;
        int threadCount = 8;
        int putsPerThread = 200;
        LruCache<Integer, String> cache = new LruCache<>(capacity);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch done = new CountDownLatch(threadCount);
        AtomicInteger keySource = new AtomicInteger();

        for (int t = 0; t < threadCount; t++) {
            executor.submit(() -> {
                try {
                    for (int i = 0; i < putsPerThread; i++) {
                        int key = keySource.incrementAndGet();
                        cache.put(key, "value-" + key);
                        cache.get(key);
                    }
                } finally {
                    done.countDown();
                }
            });
        }

        assertTrue(done.await(10, TimeUnit.SECONDS), "worker threads did not finish in time");
        executor.shutdown();

        assertTrue(cache.size() <= capacity, "cache size " + cache.size() + " must never exceed capacity " + capacity);
    }
}
