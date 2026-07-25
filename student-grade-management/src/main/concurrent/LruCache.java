package main.concurrent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A thread-safe, fixed-capacity cache with least-recently-used eviction (US-8/PBI-8), backed by a
 * {@link ConcurrentHashMap} rather than a synchronized {@code LinkedHashMap} (the more common
 * textbook LRU idiom) - every {@link #get}/{@link #put}/{@link #invalidate} is lock-free on the
 * map itself. Recency is tracked per entry via a shared {@link AtomicLong} clock rather than a
 * second, separately-synchronized ordering structure, so eviction never needs its own lock: a put()
 * over capacity scans the (weakly-consistent, safe-to-iterate-while-modified) entry set for the
 * least-recently-used key and removes it.
 *
 * @param <K> cache key type
 * @param <V> cached value type
 */
public class LruCache<K, V> {

    public static final int DEFAULT_CAPACITY = 100;

    private final int capacity;
    private final ConcurrentHashMap<K, Entry<V>> store = new ConcurrentHashMap<>();
    private final AtomicLong clock = new AtomicLong();
    private final AtomicLong hits = new AtomicLong();
    private final AtomicLong misses = new AtomicLong();

    public LruCache() {
        this(DEFAULT_CAPACITY);
    }

    public LruCache(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive, got: " + capacity);
        }
        this.capacity = capacity;
    }

    /**
     * @return the cached value, or {@code null} on a miss - also counts towards {@link #hitRate()}.
     * A {@code null} key is always a miss ({@link ConcurrentHashMap} can't store one anyway).
     */
    public V get(K key) {
        if (key == null) {
            misses.incrementAndGet();
            return null;
        }
        Entry<V> entry = store.get(key);
        if (entry == null) {
            misses.incrementAndGet();
            return null;
        }
        hits.incrementAndGet();
        entry.lastAccessed.set(clock.incrementAndGet());
        return entry.value;
    }

    /** A {@code null} key is silently not cached, rather than throwing - a cache is never required to remember every write. */
    public void put(K key, V value) {
        if (key == null) {
            return;
        }
        store.put(key, new Entry<>(value, clock.incrementAndGet()));
        evictIfOverCapacity();
    }

    /** Removes {@code key} if present; a no-op otherwise (including for a {@code null} key). This is how a write invalidates a stale cached read. */
    public void invalidate(K key) {
        if (key == null) {
            return;
        }
        store.remove(key);
    }

    public void clear() {
        store.clear();
    }

    public int size() {
        return store.size();
    }

    /** Hits / (hits + misses) across every {@link #get} call so far; {@code 0.0} before the first one. */
    public double hitRate() {
        long h = hits.get();
        long total = h + misses.get();
        return total == 0 ? 0.0 : (double) h / total;
    }

    private void evictIfOverCapacity() {
        while (store.size() > capacity) {
            K oldestKey = findLeastRecentlyUsedKey();
            if (oldestKey == null) {
                return;
            }
            store.remove(oldestKey);
        }
    }

    private K findLeastRecentlyUsedKey() {
        K oldestKey = null;
        long oldestTime = Long.MAX_VALUE;
        for (Map.Entry<K, Entry<V>> e : store.entrySet()) {
            long accessed = e.getValue().lastAccessed.get();
            if (accessed < oldestTime) {
                oldestTime = accessed;
                oldestKey = e.getKey();
            }
        }
        return oldestKey;
    }

    private static final class Entry<V> {
        private final V value;
        private final AtomicLong lastAccessed;

        Entry(V value, long lastAccessed) {
            this.value = value;
            this.lastAccessed = new AtomicLong(lastAccessed);
        }
    }
}
