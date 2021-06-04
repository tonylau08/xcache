package com.igeeksky.xcache.support.redis;

import com.igeeksky.xcache.core.sync.SyncCache;
import com.igeeksky.xcache.core.ValueWrapper;

import java.util.Map;
import java.util.Set;

/**
 * @author Patrick.Lau
 * @date 2021-06-03
 */
public class SyncRedisCache<K, V> implements SyncCache<K, V> {


    @Override
    public ValueWrapper<V> get(K key) {
        return null;
    }

    @Override
    public Map<K, V> getAll(Set<? extends K> keys) {
        return null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {

    }

    @Override
    public void put(K key, V value) {

    }

    @Override
    public ValueWrapper<V> putIfAbsent(K key, V value) {
        return null;
    }

    @Override
    public void remove(K key) {

    }

    @Override
    public void clear() {

    }
}
