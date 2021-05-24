package com.igeeksky.xcache.extend.caffeine;



import com.igeeksky.xcache.core.AbstractCacheStore;
import com.igeeksky.xcache.core.KeyValue;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;


public class CaffeineCacheStore<K,V> extends AbstractCacheStore<K,V> {

    public CaffeineCacheStore(String name, long expiration) {
        super(name, expiration);
    }

    @Override
    protected ValueWrapper<V> fromStore(K key) {
        return null;
    }


    @Override
    public CompletableFuture<ValueWrapper<V>> asyncGet(K key) {
        return null;
    }

    @Override
    public CompletableFuture<V> asyncGet(K key, Class<V> type) {
        return null;
    }

    @Override
    public void put(K key, V value) {

    }


    @Override
    public ValueWrapper putIfAbsent(K key, V value) {
        return null;
    }

    @Override
    public void evict(Object key) {

    }

    @Override
    public void clear() {

    }
}
