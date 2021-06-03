package com.igeeksky.xcache.core;

import java.util.Map;
import java.util.Set;

/**
 * @author Patrick.Lau
 * @date 2021-06-03
 */
public class CompositeCache<K, V> implements Cache<K, V> {

    private final Cache<K, V> localCache;
    private final Cache<K, V> remoteCache;

    public CompositeCache(Cache<K, V> localCache, Cache<K, V> remoteCache) {
        this.localCache = localCache;
        this.remoteCache = remoteCache;
    }

    @Override
    public ValueWrapper<V> get(K key) {

        return null;
    }

    @Override
    public Map<K, V> getAll(Set<? extends K> keys) {
        return null;
    }

    @Override
    public void put(K key, V value) {

    }

    @Override
    public ValueWrapper<V> putIfAbsent(K key, V value) {
        return null;
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
}
