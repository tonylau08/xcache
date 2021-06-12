package com.igeeksky.xcache.core;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * @author Patrick.Lau
 * @date 2020-12-11
 */
public interface SyncCache<K, V> {

    CacheValue<V> get(K key);

    CacheValue<V> get(K key, Callable<V> loader);

    Map<K, CacheValue<V>> getAll(Set<? extends K> keys);

    default boolean containsKey(K key) {
        return true;
    }

    void put(K key, V value);

    void putAll(Map<? extends K, ? extends V> map);

    void remove(K key);

    void clear();

}
