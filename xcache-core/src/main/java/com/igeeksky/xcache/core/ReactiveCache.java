package com.igeeksky.xcache.core;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Set;

/**
 * @author Patrick.Lau
 * @date 2021-06-03
 */
public interface ReactiveCache<K, V> {

    Mono<CacheValue<V>> reactiveGet(K key);

    Flux<KeyValue<K, CacheValue<V>>> reactiveGetAll(Set<? extends K> keys);

    Mono<Void> reactivePutAll(Mono<Map<? extends K, ? extends V>> keyValues);

    Mono<Void> reactivePut(K key, Mono<V> value);

    Mono<Void> reactiveRemove(K key);

}
