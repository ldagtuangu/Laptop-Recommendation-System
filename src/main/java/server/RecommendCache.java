package server;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory cache dùng ConcurrentHashMap
 *
 * Key   = cache key từ UserPreference
 * Value = JSON response string đã tính sẵn
 *
 * Features:
 *   - Thread-safe (ConcurrentHashMap)
 *   - TTL (Time To Live) — tự xóa entry hết hạn
 *   - Max size — tự xóa entry cũ nhất khi đầy
 *   - Hit/miss stats
 */
public class RecommendCache {

    private static final int    MAX_SIZE    = 100;
    private static final long   TTL_MS      = 5 * 60 * 1000L;

    private final ConcurrentHashMap<String, CacheEntry> cache
            = new ConcurrentHashMap<>();

    private int hits   = 0;
    private int misses = 0;

    // ── Cache entry (value + expiry time) ────────────────────────────────────
    private static class CacheEntry {
        final String value;
        final long   expireAt;

        CacheEntry(String value) {
            this.value    = value;
            this.expireAt = System.currentTimeMillis() + TTL_MS;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expireAt;
        }
    }

    // ── Get ───────────────────────────────────────────────────────────────────
    public Optional<String> get(String key) {
        CacheEntry entry = cache.get(key);

        if (entry == null) {
            misses++;
            return Optional.empty();
        }

        if (entry.isExpired()) {
            cache.remove(key);
            misses++;
            return Optional.empty();
        }

        hits++;
        return Optional.of(entry.value);
    }

    // ── Put ───────────────────────────────────────────────────────────────────
    public void put(String key, String value) {
        // Nếu đầy → xóa 10 entry cũ nhất
        if (cache.size() >= MAX_SIZE) {
            evict(10);
        }
        cache.put(key, new CacheEntry(value));
    }

    // ── Evict oldest entries ──────────────────────────────────────────────────
    private void evict(int count) {
        cache.entrySet().stream()
                .sorted(Comparator.comparingLong(e -> e.getValue().expireAt))
                .limit(count)
                .forEach(e -> cache.remove(e.getKey()));
        System.out.println("Cache evicted " + count + " entries.");
    }

    // ── Clear expired ─────────────────────────────────────────────────────────
    public void clearExpired() {
        int before = cache.size();
        cache.entrySet().removeIf(e -> e.getValue().isExpired());
        int removed = before - cache.size();
        if (removed > 0) System.out.println("Cache cleared " + removed + " expired entries.");
    }

    // ── Stats ─────────────────────────────────────────────────────────────────
    public void printStats() {
        int total = hits + misses;
        double rate = total == 0 ? 0 : (hits * 100.0 / total);
        System.out.printf("Cache stats — size=%d hits=%d misses=%d hitRate=%.1f%%%n",
                cache.size(), hits, misses, rate);
    }

    // ── Cache key from preference ─────────────────────────────────────────────
    public static String buildKey(String category, String cpuBrand,
                                  Boolean wantGpu,
                                  double weightPerf, double weightPort,
                                  double weightDisp, int topN) {
        return String.format("%s:%s:%s:%.1f:%.1f:%.1f:%d",
                category, cpuBrand, wantGpu,
                weightPerf, weightPort, weightDisp, topN);
    }
}