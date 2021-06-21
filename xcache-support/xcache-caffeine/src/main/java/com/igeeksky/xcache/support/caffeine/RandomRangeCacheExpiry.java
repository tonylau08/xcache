package com.igeeksky.xcache.support.caffeine;

import com.github.benmanes.caffeine.cache.Expiry;
import com.igeeksky.xcache.core.ExpiryCacheValue;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author: Patrick.Lau
 * @date: 2021-06-16
 */
public class RandomRangeCacheExpiry<K, V> implements Expiry<K, ExpiryCacheValue<V>> {

    private long originExpireAfterCreateNanos;
    private long nullValueExpireAfterCreateNanos;
    private long expireAfterCreateNanos;
    private long expiresAfterAccessNanos;

    public RandomRangeCacheExpiry(long expireAfterCreateNanos, long expiresAfterAccessNanos) {
        this.expireAfterCreateNanos = expireAfterCreateNanos;
        this.originExpireAfterCreateNanos = expireAfterCreateNanos - (long) (expireAfterCreateNanos * 0.1);
        this.nullValueExpireAfterCreateNanos = (long) (expireAfterCreateNanos * 0.5);
        this.expiresAfterAccessNanos = expiresAfterAccessNanos;
    }

    @Override
    public long expireAfterCreate(@NonNull K key, @NonNull ExpiryCacheValue<V> cacheValue, long currentTime) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        long duration = (cacheValue.getValue() != null) ?
                random.nextLong(originExpireAfterCreateNanos, expireAfterCreateNanos)
                : random.nextLong(nullValueExpireAfterCreateNanos, originExpireAfterCreateNanos);
        cacheValue.setExpiryTime(currentTime + duration);
        return duration;
    }

    @Override
    public long expireAfterUpdate(@NonNull K key, @NonNull ExpiryCacheValue<V> cacheValue,
                                  long currentTime, @NonNegative long currentDuration) {
        return expireAfterCreate(key, cacheValue, currentTime);
    }

    @Override
    public long expireAfterRead(@NonNull K key, @NonNull ExpiryCacheValue<V> cacheValue,
                                long currentTime, @NonNegative long currentDuration) {
        long duration = cacheValue.getExpiryTime() - currentTime;
        if (duration < 0) {
            return 0L;
        }
        return (duration > expiresAfterAccessNanos) ? expiresAfterAccessNanos : duration;
    }
}
