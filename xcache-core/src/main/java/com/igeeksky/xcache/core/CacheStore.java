package com.igeeksky.xcache.core;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Set;

/**
 * @author Patrick.Lau
 * @date 2021-06-01
 */
public interface CacheStore<K, V> {

    Mono<CacheValue<V>> get(K key);

    Flux<KeyValue<K, CacheValue<V>>> getAll(Set<? extends K> keys);

    Mono<CacheValue<V>> put(K key, V value);

    Mono<Void> putAll(Mono<Map<? extends K, ? extends V>> keyValues);

    Mono<Void> remove(K key);

    default void clear() {
        // do nothing
    }

}
