package com.igeeksky.xcache.core.async;

import com.igeeksky.xcache.core.ValueWrapper;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionStage;

/**
 * @author Patrick.Lau
 * @date 2021-06-04
 */
public class AsyncCompositeCache<K, V> implements AsyncCache<K, V> {

    private final AsyncCache<K, V> localCache;
    private final AsyncCache<K, V> remoteCache;

    public AsyncCompositeCache(AsyncCache<K, V> localCache, AsyncCache<K, V> remoteCache) {
        this.localCache = localCache;
        this.remoteCache = remoteCache;
    }

    @Override
    public CompletionStage<ValueWrapper<V>> get(K key) {
        return null;
    }

    @Override
    public CompletionStage<Map<K, V>> getAll(Set<? extends K> keys) {
        return null;
    }

    @Override
    public CompletionStage<Void> putAll(Map<? extends K, ? extends V> map) {
        return null;
    }

    @Override
    public CompletionStage<Void> put(K key, V value) {
        return null;
    }

    @Override
    public CompletionStage<ValueWrapper<V>> putIfAbsent(K key, V value) {
        return null;
    }

    @Override
    public CompletionStage<Void> remove(K key) {
        return null;
    }

    @Override
    public CompletionStage<Void> clear() {
        return null;
    }
}
