package com.igeeksky.xcache.core;

import com.igeeksky.xcache.core.async.AsyncCache;
import com.igeeksky.xcache.core.reactive.ReactiveCache;
import com.igeeksky.xcache.core.sync.SyncCache;

/**
 * @author Patrick.Lau
 * @date 2021-06-04
 */
public class XcacheDefault<K, V> implements Xcache {

    private final SyncCache<K, V> syncCache;
    private final AsyncCache<K, V> asyncCache;
    private final ReactiveCache<K, V> reactiveCache;

    public XcacheDefault(SyncCache<K, V> syncCache, AsyncCache<K, V> asyncCache, ReactiveCache<K, V> reactiveCache) {
        this.syncCache = syncCache;
        this.asyncCache = asyncCache;
        this.reactiveCache = reactiveCache;
    }

    @Override
    public SyncCache<K, V> sync() {
        return syncCache;
    }

    @Override
    public AsyncCache<K, V> async() {
        return asyncCache;
    }

    @Override
    public ReactiveCache<K, V> reactive() {
        return reactiveCache;
    }
}
