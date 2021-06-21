package com.igeeksky.xcache.core;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Patrick.Lau
 * @date 2021-06-10
 */
public class DefaultCacheManager implements CacheManager {

    protected ConcurrentMap<String, Cache<Object, Object>> caches = new ConcurrentHashMap<>();
    protected CacheBuilder cacheBuilder;

    public DefaultCacheManager(CacheBuilder cacheBuilder) {
        this.cacheBuilder = cacheBuilder;
    }

    @Override
    public Cache<Object, Object> get(String name) {
        return caches.computeIfAbsent(name, cacheName -> cacheBuilder.build(cacheName));
    }

    @Override
    public <K, V> Cache<K, V> get(String name, Class<K> keyClazz, Class<V> valueClazz) {
        return cacheBuilder.build(name, keyClazz, valueClazz);
    }

    @Override
    public Collection<Cache> getAll() {
        return Collections.unmodifiableCollection(caches.values());
    }

    @Override
    public Collection<String> getAllCacheNames() {
        return Collections.unmodifiableCollection(caches.keySet());
    }
}
