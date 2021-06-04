package com.igeeksky.xcache.core.sync;

import com.igeeksky.xcache.core.ValueWrapper;

import java.util.Map;
import java.util.Set;

/**
 * @author Patrick.Lau
 * @date 2021-06-03
 */
public class CompositeSyncCache<K, V> implements SyncCache<K, V> {

    private final SyncCache<K, V> localCache;
    private final SyncCache<K, V> remoteCache;

    public CompositeSyncCache(SyncCache<K, V> localCache, SyncCache<K, V> remoteCache) {
        this.localCache = localCache;
        this.remoteCache = remoteCache;
    }

    @Override
    public ValueWrapper<V> get(K key) {
        ValueWrapper<V> localWrapper = localCache.get(key);
        if (null != localWrapper) {
            return localWrapper;
        }
        ValueWrapper<V> remoteWrapper = remoteCache.get(key);
        if (null != remoteWrapper) {
            V value = remoteWrapper.getValue();
            localCache.put(key, value);
        }
        return remoteWrapper;
    }

    @Override
    public Map<K, V> getAll(Set<? extends K> keys) {
        Map<K, V> localCacheAll = localCache.getAll(keys);
        keys.removeAll(localCacheAll.keySet());
        Map<K, V> remoteCacheAll = remoteCache.getAll(keys);
        remoteCacheAll.putAll(localCacheAll);
        return remoteCacheAll;
    }

    @Override
    public void put(K key, V value) {
        localCache.put(key, value);
        remoteCache.put(key, value);
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
