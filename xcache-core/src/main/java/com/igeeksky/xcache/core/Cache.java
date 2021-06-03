package com.igeeksky.xcache.core;

import java.util.Map;
import java.util.Set;

/**
 * @author Patrick.Lau
 * @date 2020-12-11
 */
public interface Cache<K, V> {

    ValueWrapper<V> get(K key);

    Map<K, V> getAll(Set<? extends K> keys);

    void put(K key, V value);

    ValueWrapper<V> putIfAbsent(K key, V value);

    void putAll(Map<? extends K, ? extends V> map);

    void remove(K key);

    void clear();

}
