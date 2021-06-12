package com.igeeksky.xcache.core;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Patrick.Lau
 * @date 2021-06-10
 */
public class CompositeCacheManager implements CacheManager {

    private CacheManager firstCacheManager;
    private CacheManager secondCacheManager;
    protected ConcurrentHashMap<String, XCache> caches;

    public CompositeCacheManager(CacheManager firstCacheManager, CacheManager secondCacheManager) {
        this.firstCacheManager = firstCacheManager;
        this.secondCacheManager = secondCacheManager;
    }

    @Override
    public XCache get(String name) {
        return caches.computeIfAbsent(name, key -> {
            XCache firstCache = firstCacheManager.get(key);
            XCache secondCache = secondCacheManager.get(key);
            return new CompositeCache(name, firstCache, secondCache);
        });
    }

    @Override
    public Collection<XCache> getAll() {
        return null;
    }

    @Override
    public Collection<String> getAllCacheNames() {
        return null;
    }
}
