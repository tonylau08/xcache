package com.igeeksky.xcache.core;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Patrick.Lau
 * @date 2021-06-10
 */
public class DefaultCacheManager implements CacheManager {

    protected CacheLevel cacheLevel;
    protected String namespace;
    protected ConcurrentMap<String, Cache<?, ?>> caches = new ConcurrentHashMap<>();
    protected CacheBuilder cacheBuilder;

    public DefaultCacheManager(String namespace, CacheLevel cacheLevel, CacheBuilder cacheBuilder) {
        this.namespace = namespace;
        this.cacheLevel = cacheLevel;
        this.cacheBuilder = cacheBuilder;
    }

    @Override
    public CacheLevel getCacheLevel() {
        return cacheLevel;
    }

    @Override
    public String getNamespace() {
        return null;
    }

    @Override
    public Cache<Object, Object> get(String name) {
        return get(name, Object.class, Object.class);
    }

    @Override
    public <K, V> Cache<K, V> get(String name, Class<K> keyClazz, Class<V> valueClazz) {
        return (Cache<K, V>) caches.computeIfAbsent(name, k -> cacheBuilder.build(k, keyClazz, valueClazz));
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
