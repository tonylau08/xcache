package com.igeeksky.xcache.core;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Patrick.Lau
 * @date 2021-06-10
 */
public class CompositeCacheManager implements CacheManager {

    private final CacheManager firstCacheManager;
    private final CacheManager secondCacheManager;
    protected ConcurrentHashMap<String, Cache<?, ?>> caches;

    public CompositeCacheManager(CacheManager firstCacheManager, CacheManager secondCacheManager) {
        this.firstCacheManager = firstCacheManager;
        this.secondCacheManager = secondCacheManager;
    }

    @Override
    public Cache<?, ?> get(String name) {
        return caches.computeIfAbsent(name, key -> {
            Cache<?, ?> firstCache = firstCacheManager.get(key);
            Cache<?, ?> secondCache = secondCacheManager.get(key);
            return new CompositeCache(name, firstCache, secondCache);
        });
    }

    @Override
    public Collection<Cache> getAll() {
        return null;
    }

    @Override
    public Collection<String> getAllCacheNames() {
        return null;
    }
}
