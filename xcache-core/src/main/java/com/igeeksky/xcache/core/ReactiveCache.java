package com.igeeksky.xcache.core;

import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Set;

/**
 * @author Patrick.Lau
 * @date 2021-06-03
 */
public interface ReactiveCache<K, V> {

    Mono<ValueWrapper<V>> get(K key);

    Mono<Map<K, V>> getAll(Set<? extends K> keys);

    Mono<Void> putAll(java.util.Map<? extends K, ? extends V> map);

    Mono<Void> put(K key, V value);

    Mono<ValueWrapper<V>> putIfAbsent(K key, V value);

    Mono<Void> remove(K key);

    Mono<Void> clear();

}
