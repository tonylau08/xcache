package com.igeeksky.xcache.core;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * @author Patrick.Lau
 * @date 2021-06-03
 */
public class CompositeCache<K, V> implements Cache<K, V> {

    private final String name;
    private final Cache<K, V> firstCache;
    private final Cache<K, V> secondCache;

    public CompositeCache(String name, Cache<K, V> firstCache, Cache<K, V> secondCache) {
        this.name = name;
        this.firstCache = firstCache;
        this.secondCache = secondCache;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Mono<CacheValue<V>> get(K key) {
        return null;
    }

    @Override
    public Mono<CacheValue<V>> get(K key, Callable<V> loader) {
        return null;
    }

    @Override
    public Flux<KeyValue<K, CacheValue<V>>> getAll(Set<? extends K> keys) {
        return null;
    }

    @Override
    public Mono<Void> putAll(Mono<Map<? extends K, ? extends V>> keyValues) {
        return null;
    }

    @Override
    public Mono<Void> put(K key, Mono<V> value) {
        return null;
    }

    @Override
    public Mono<Void> remove(K key) {
        return null;
    }

    @Override
    public void clear() {

    }

    @Override
    public SyncCache<K, V> sync() {
        return null;
    }

    @Override
    public AsyncCache<K, V> async() {
        return null;
    }
}
