package com.igeeksky.xcache.core;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Closeable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * @author Patrick.Lau
 */
public interface CacheStore<K, V> extends Closeable {

    CacheValue<V> get(K key);

    Map<K, CacheValue<V>> getAll(Set<? extends K> keys);

    CacheValue<V> put(K key, V value);

    void putAll(Map<? extends K, ? extends V> keyValues);

    void remove(K key);

    void clear();

    CompletableFuture<CacheValue<V>> asyncGet(K key);

    Map<K, CompletableFuture<CacheValue<V>>> asyncGetAll(Set<? extends K> keys);

    CompletableFuture<Void> asyncPutAll(CompletableFuture<Map<? extends K, ? extends V>> keyValuesFuture);

    CompletableFuture<Void> asyncPut(K key, CompletableFuture<V> valueFuture);

    CompletableFuture<Void> asyncRemove(K key);

    Mono<CacheValue<V>> reactiveGet(K key);

    Flux<KeyValue<K,CacheValue<V>>> reactiveGetAll(Set<? extends K> keys);

    Mono<Void> reactivePutAll(Mono<Map<? extends K, ? extends V>> keyValues);

    Mono<Void> reactivePut(K key, Mono<V> value);

    Mono<Void> reactiveRemove(K key);

}
