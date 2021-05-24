package com.igeeksky.xcache.extend.caffeine;


import com.igeeksky.xcache.core.AbstractCacheStore;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;


public class CaffeineCacheStore extends AbstractCacheStore {

    public CaffeineCacheStore(String name, long expireAfterWrite) {
        super(name, expireAfterWrite);
    }

    @Override
    protected  <T> ValueWrapper<T> fromStore(Object key) {
        return null;
    }

    @Override
    public <T> CompletionStage<ValueWrapper<T>> asyncGet(Object key) {
        return null;
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        return null;
    }

    @Override
    public <T> CompletableFuture<ValueWrapper<T>> asyncGet(Object key, Class<T> type) {
        return null;
    }

    @Override
    public <T> void put(Object key, T value) {

    }

    @Override
    public <T> ValueWrapper<T> putIfAbsent(Object key, T value) {
        return null;
    }

    @Override
    public void evict(Object key) {

    }

    @Override
    public void clear() {

    }
}
