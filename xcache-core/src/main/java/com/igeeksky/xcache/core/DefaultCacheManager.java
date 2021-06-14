package com.igeeksky.xcache.core;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Patrick.Lau
 * @date 2021-06-10
 */
public class DefaultCacheManager implements CacheManager {

    protected ConcurrentMap<String, Cache<?, ?>> caches = new ConcurrentHashMap<>();
    protected CacheBuilder cacheBuilder;

    @Override
    public Cache get(String name) {
        return caches.computeIfAbsent(name, cacheName -> cacheBuilder.build(cacheName));
    }

    @Override
    public Collection<Cache> getAll() {
        return null;
    }

    @Override
    public Collection<String> getAllCacheNames() {
        return new HashSet<>(caches.keySet());
    }
}
