package com.igeeksky.xcache.core;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * @author Patrick.Lau
 * @date 2020-12-11
 */
public interface CacheStore<K, V> {

    // TODO 异步API
    // TODO 本地缓存更新事件传播
    String getName();

    ValueWrapper<V> get(K key);



    V get(K key, Class<V> type);

    CompletableFuture<V> asyncGet(K key, Class<V> type);

    void put(K key, V value);

    ValueWrapper putIfAbsent(K key, V value);

    void evict(K key);

    void clear();

    interface ValueWrapper<V> {
        V getValue();
    }

}
