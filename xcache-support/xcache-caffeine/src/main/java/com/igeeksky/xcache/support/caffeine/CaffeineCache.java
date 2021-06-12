package com.igeeksky.xcache.support.caffeine;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.igeeksky.xcache.core.CacheStore;
import com.igeeksky.xcache.core.CacheValue;
import com.igeeksky.xcache.core.KeyValue;
import com.igeeksky.xcache.core.SimpleCacheValue;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author: Patrick.Lau
 * @date: 2021-06-11
 */
public class CaffeineCache<K, V> implements CacheStore<K, V> {

    private final Logger log = LoggerFactory.getLogger(CaffeineCache.class);

    AsyncCache<K, CacheValue<V>> asyncCache = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(128)
            .buildAsync();

    @Override
    public CacheValue<V> get(K key) {
        return asyncCache.synchronous().getIfPresent(key);
    }

    @Override
    public Map<K, CacheValue<V>> getAll(Set<? extends K> keys) {
        return asyncCache.synchronous().getAllPresent(keys);
    }

    @Override
    public CacheValue<V> put(K key, V value) {
        CacheValue<V> cacheValue = toCacheValue(value);
        asyncCache.synchronous().put(key, cacheValue);
        return cacheValue;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> keyValues) {
        Map<K, CacheValue<V>> cacheValueMap = new HashMap<>();
        keyValues.forEach((k, v) -> {
            cacheValueMap.put(k, toCacheValue(v));
        });
        asyncCache.synchronous().putAll(cacheValueMap);
    }

    @NotNull
    private SimpleCacheValue toCacheValue(V v) {
        return new SimpleCacheValue<>(v);
    }

    @Override
    public void remove(K key) {
        asyncCache.synchronous().invalidate(key);
    }

    @Override
    public void clear() {
        asyncCache.synchronous().invalidateAll();
    }

    @Override
    public CompletableFuture<CacheValue<V>> asyncGet(K key) {
        return asyncCache.getIfPresent(key);
    }

    @Override
    public CompletableFuture<Map<K, CacheValue<V>>> asyncGetAll(Set<? extends K> keys) {
        return CompletableFuture.completedFuture(getAll(keys));
    }

    @Override
    public CompletableFuture<Void> asyncPutAll(CompletableFuture<Map<? extends K, ? extends V>> keyValues) {
        return keyValues.thenApply(map -> {
            Map<K, CompletableFuture<CacheValue<V>>> futureMap = new HashMap<>(map.size());
            map.forEach((k, v) -> {
                futureMap.put(k, CompletableFuture.completedFuture(toCacheValue(v)));
            });
            return futureMap;
        }).thenApply(futureMap -> {
            asyncCache.asMap().putAll(futureMap);
            return null;
        });
    }

    @Override
    public CompletableFuture<Void> asyncPut(K key, CompletableFuture<V> valueFuture) {
        return valueFuture.thenApply(this::toCacheValue).thenCompose(vCacheValue -> {
            return asyncCache.asMap().put(key, CompletableFuture.completedFuture(vCacheValue)).thenApply(v -> null);
        });
    }

    @Override
    public CompletableFuture<Void> asyncRemove(K key) {
        return asyncCache.asMap().remove(key).thenApply(vCacheValue -> null);
    }

    @Override
    public Mono<CacheValue<V>> reactiveGet(K key) {
        return Mono.fromCompletionStage(asyncGet(key));
    }

    @Override
    public Flux<KeyValue<K, CacheValue<V>>> reactiveGetAll(Set<? extends K> keys) {
        return Flux.fromIterable(keys).map(key -> {
            CompletableFuture<CacheValue<V>> future = asyncCache.getIfPresent(key);
            try {
                return new KeyValue(key, future.get());
            } catch (Exception e) {
                log.error("Future get error.key={}", key, e);
            }
            return null;
        });
    }

    @Override
    public Mono<Void> reactivePutAll(Mono<Map<? extends K, ? extends V>> keyValues) {
        return null;
    }

    @Override
    public Mono<Void> reactivePut(K key, Mono<V> value) {
        return null;
    }

    @Override
    public Mono<Void> reactiveRemove(K key) {
        return null;
    }

    @Override
    public void close() throws IOException {

    }
}
