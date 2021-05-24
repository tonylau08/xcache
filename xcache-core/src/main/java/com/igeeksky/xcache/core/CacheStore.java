package com.igeeksky.xcache.core;

import java.util.concurrent.CompletableFuture;

/**
 * @author Patrick.Lau
 * @date 2020-12-11
 */
public interface CacheStore {

    String getName();

    <T> ValueWrapper<T> get(Object key);

    <T> T get(Object key, Class<T> type);

    <T> CompletableFuture<ValueWrapper<T>> asyncGet(Object key, Class<T> type);

    <T> void put(Object key, T value);

    <T> ValueWrapper<T> putIfAbsent(Object key, T value);

    void evict(Object key);

    void clear();

    interface ValueWrapper<T> {
        T getValue();
    }

}
