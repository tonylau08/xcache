package com.igeeksky.xcache.core;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Patrick.Lau
 * @date 2021-06-10
 */
public class XCacheManager implements CacheManager {

    protected ConcurrentHashMap<String, XCache> caches;
    protected CacheBuilder cacheBuilder;

    @Override
    public XCache get(String name) {
        return caches.computeIfAbsent(name, key -> cacheBuilder.build(key));
    }

    @Override
    public Collection<XCache> getAll() {
        return null;
    }

    @Override
    public Collection<String> getAllCacheNames() {
        return new HashSet<>(caches.keySet());
    }
}
