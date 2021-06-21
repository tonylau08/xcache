package com.igeeksky.xcache.core;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Patrick.Lau
 * @date 2021-06-10
 */
public class CompositeCacheManager implements CacheManager {

    private final CacheManager firstCacheManager;
    private final CacheManager secondCacheManager;
    protected ConcurrentMap<String, Cache<?, ?>> caches = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, CacheUse> cacheUseConfig = new ConcurrentHashMap<>();

    public CompositeCacheManager(CacheManager firstCacheManager, CacheManager secondCacheManager) {
        this.firstCacheManager = firstCacheManager;
        this.secondCacheManager = secondCacheManager;
    }

    @Override
    public Cache<Object, Object> get(String name) {
        return get(name, Object.class, Object.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <K, V> Cache<K, V> get(String name, Class<K> keyClazz, Class<V> valueClazz) {
        Cache<?, ?> cache = caches.computeIfAbsent(name, key -> {
            CacheUse cacheUse = cacheUseConfig.get(name);
            if (CacheUse.FIRST == cacheUse) {
                return firstCacheManager.get(key, keyClazz, valueClazz);
            } else if (CacheUse.SECOND == cacheUse) {
                return secondCacheManager.get(key, keyClazz, valueClazz);
            } else {
                Cache<K, V> firstCache = firstCacheManager.get(key, keyClazz, valueClazz);
                Cache<K, V> secondCache = secondCacheManager.get(key, keyClazz, valueClazz);
                return new CompositeCache<>(name, firstCache, secondCache);
            }
        });
        return (Cache<K, V>) cache;
    }

    public void setCacheUse(String name, CacheUse cacheUse) {
        cacheUseConfig.put(name, cacheUse);
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
