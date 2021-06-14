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
public interface ReactiveCache<K, V> {

    Mono<CacheValue<V>> get(K key);

    Mono<CacheValue<V>> get(K key, Callable<V> loader);

    Flux<KeyValue<K, CacheValue<V>>> getAll(Set<? extends K> keys);

    Mono<Void> putAll(Mono<Map<? extends K, ? extends V>> keyValues);

    Mono<Void> put(K key, Mono<V> value);

    Mono<Void> remove(K key);

    void clear();

    default Mono<Boolean> containsKey(K key) {
        return Mono.just(Boolean.TRUE);
    }



}
