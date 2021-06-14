package com.igeeksky.xcache.support.caffeine;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.igeeksky.xcache.core.CacheStore;
import com.igeeksky.xcache.core.CacheValue;
import com.igeeksky.xcache.core.KeyValue;
import com.igeeksky.xcache.core.SimpleCacheValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author: Patrick.Lau
 * @date: 2021-06-11
 */
public class CaffeineCacheStore<K, V> implements CacheStore<K, V> {

    private final Logger log = LoggerFactory.getLogger(CaffeineCacheStore.class);

    private final Cache<K, CacheValue<V>> cache = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(128)
            .build();

    @Override
    public Mono<CacheValue<V>> get(K key) {
        return Mono.justOrEmpty(cache.getIfPresent(key));
    }

    @Override
    public Flux<KeyValue<K, CacheValue<V>>> getAll(Set<? extends K> keys) {
        return Flux.fromIterable(keys)
                .map(key -> new KeyValue<>(key, cache.getIfPresent(key)));
    }

    @Override
    public Mono<CacheValue<V>> put(K key, Mono<V> value) {
        return value.map(v -> {
            CacheValue<V> cacheValue = toCacheValue(v);
            cache.put(key, cacheValue);
            return cacheValue;
        });
    }

    @Override
    public Mono<Void> putAll(Mono<Map<? extends K, ? extends V>> mono) {
        return mono.flatMap(map -> {
            map.forEach((k, v) -> {
                cache.put(k, toCacheValue(v));
            });
            return Mono.empty();
        });
    }

    @Override
    public Mono<Void> remove(K key) {
        return Mono.justOrEmpty(cache.asMap().remove(key)).then();
    }

    @Override
    public void clear() {
        cache.asMap().clear();
    }

    private SimpleCacheValue<V> toCacheValue(V v) {
        return new SimpleCacheValue<>(v);
    }

}
