package com.igeeksky.xcache.core;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionStage;

/**
 * @author Patrick.Lau
 * @date 2020-12-11
 */
public interface Cache<K, V> {

    String getName();

    ValueWrapper<V> get(K key);

    Map<K, V> getAll(Set<? extends K> keys);

    void putAll(java.util.Map<? extends K, ? extends V> map);

    CompletionStage<ValueWrapper<V>> asyncGet(K key);

    void put(K key, V value);

    ValueWrapper<V> putIfAbsent(K key, V value);

    void remove(K key);

    void clear();

    interface ValueWrapper<V> {
        V getValue();
    }

}
