package com.igeeksky.xcache.support.caffeine;

import com.github.benmanes.caffeine.cache.Cache;
import com.igeeksky.xcache.core.CacheStore;
import com.igeeksky.xcache.core.CacheValue;
import com.igeeksky.xcache.core.KeyValue;
import com.igeeksky.xcache.core.ExpiryCacheValue;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Set;

/**
 * @author: Patrick.Lau
 * @date: 2021-06-11
 */
public class CaffeineCacheStore<K, V> implements CacheStore<K, V> {

    private final Cache<K, ExpiryCacheValue<V>> cache;

    public CaffeineCacheStore(Cache<K, ExpiryCacheValue<V>> cache) {
        this.cache = cache;
    }

    @Override
    public Mono<CacheValue<V>> get(K key) {
        return Mono.justOrEmpty(cache.getIfPresent(key));
    }

    @Override
    public Flux<KeyValue<K, CacheValue<V>>> getAll(Set<? extends K> keys) {
        return Flux.fromIterable(keys)
                .map(key -> new KeyValue<K, CacheValue<V>>(key, cache.getIfPresent(key)))
                .filter(kv -> kv.hasValue());
    }

    @Override
    public Mono<CacheValue<V>> put(K key, V value) {
        return Mono.fromSupplier(() -> {
            ExpiryCacheValue<V> cacheValue = toCacheValue(value);
            cache.put(key, cacheValue);
            return cacheValue;
        });
    }

    @Override
    public Mono<Void> putAll(Mono<Map<? extends K, ? extends V>> keyValues) {
        return keyValues.flatMap(map -> {
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

    private ExpiryCacheValue<V> toCacheValue(V v) {
        return new ExpiryCacheValue<>(v);
    }

}
