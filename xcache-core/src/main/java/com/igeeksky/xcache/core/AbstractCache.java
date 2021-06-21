package com.igeeksky.xcache.core;

/**
 * @author: Patrick.Lau
 * @date: 2021-06-19
 */
public abstract class AbstractCache<K, V> implements Cache<K, V> {

    private final String name;
    private volatile SyncCache<K, V> syncCache;
    private volatile AsyncCache<K, V> asyncCache;

    protected final Object lock = new Object();

    public AbstractCache(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public SyncCache<K, V> sync() {
        if (null == syncCache) {
            synchronized (lock) {
                if (null == syncCache) {
                    this.syncCache = new SyncCache.SyncCacheView<>(this);
                }
            }
        }
        return syncCache;
    }

    @Override
    public AsyncCache<K, V> async() {
        if (null == asyncCache) {
            synchronized (lock) {
                if (null == asyncCache) {
                    this.asyncCache = new AsyncCache.AsyncCacheView<>(this);
                }
            }
        }
        return asyncCache;
    }
}
