package com.igeeksky.xcache.core;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author Patrick.Lau
 * @date 2021-06-03
 */
public class CompositeCache<K, V> implements XCache<K, V> {

    private final String name;
    private final XCache<K, V> firstCache;
    private final XCache<K, V> secondCache;

    public CompositeCache(String name, XCache<K, V> firstCache, XCache<K, V> secondCache) {
        this.name = name;
        this.firstCache = firstCache;
        this.secondCache = secondCache;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public CacheValue<V> get(K key) {
        return null;
    }

    @Override
    public CacheValue<V> get(K key, Callable<V> loader) {
        return null;
    }

    @Override
    public Map<K, CacheValue<V>> getAll(Set<? extends K> keys) {
        return null;
    }

    @Override
    public boolean containsKey(K key) {
        return XCache.super.containsKey(key);
    }

    @Override
    public void put(K key, V value) {

    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {

    }

    @Override
    public void remove(K key) {

    }

    @Override
    public void clear() {

    }

    @Override
    public CompletableFuture<CacheValue<V>> asyncGet(K key) {
        return null;
    }

    @Override
    public CompletableFuture<Map<K, CacheValue<V>>> asyncGetAll(Set<? extends K> keys) {
        return null;
    }

    @Override
    public CompletableFuture<Void> asyncPutAll(CompletableFuture<Map<? extends K, ? extends V>> keyValuesFuture) {
        return null;
    }

    @Override
    public CompletableFuture<Void> asyncPut(K key, CompletableFuture<V> valueFuture) {
        return null;
    }

    @Override
    public CompletableFuture<Void> asyncRemove(K key) {
        return null;
    }

    @Override
    public Mono<CacheValue<V>> reactiveGet(K key) {
        return null;
    }

    @Override
    public Flux<KeyValue<K, CacheValue<V>>> reactiveGetAll(Set<? extends K> keys) {
        return null;
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

}
