package com.igeeksky.xcache.core;

import com.igeeksky.xcache.core.util.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * @author Patrick.Lau
 * @date 2021-06-10
 */
public class CompositeCacheManager implements CacheManager {

    protected ConcurrentMap<String, Cache<?, ?>> caches = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, String> cacheUseConfig = new ConcurrentHashMap<>();
    private final ConcurrentMap<CacheLevel, CacheManager> cacheManagerMap = new ConcurrentHashMap<>(16);

    public CompositeCacheManager(List<CacheManager> cacheManagers) {
        for (CacheManager cacheManager : cacheManagers) {
            if (null != cacheManager) {
                cacheManagerMap.put(cacheManager.getCacheLevel(), cacheManager);
            }
        }
    }

    @Override
    public CacheLevel getCacheLevel() {
        return CacheLevel.L0;
    }

    @Override
    public String getNamespace() {
        return null;
    }

    @Override
    public Cache<Object, Object> get(String name) {
        return get(name, Object.class, Object.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <K, V> Cache<K, V> get(String name, Class<K> keyClazz, Class<V> valueClazz) {
        Cache<?, ?> cache = caches.computeIfAbsent(name, key -> {
            List<Cache<K, V>> cacheList = getCacheList(key, keyClazz, valueClazz);
            return getCache(key, cacheList);
        });
        if (null == cache) {
            throw new NullPointerException("Can't init cache:" + name);
        }
        return (Cache<K, V>) cache;
    }

    @Nullable
    private <K, V> Cache<K, V> getCache(String name, List<Cache<K, V>> cacheList) {
        int size = cacheList.size();
        switch (size) {
            case 0:
                return null;
            case 1:
                return cacheList.get(0);
            case 2:
                return new CompositeCache<>(name, cacheList.get(0), cacheList.get(1));
            default: {
                List<Cache<K, V>> list = new ArrayList<>();
                Cache<K, V> first = null;
                for (Cache<K, V> cache : cacheList) {
                    if (first == null) {
                        first = cache;
                    } else {
                        list.add(new CompositeCache<>(name, first, cache));
                        first = null;
                    }
                }
                if (null != first) {
                    list.add(first);
                }
                return getCache(name, list);
            }
        }
    }

    @NotNull
    private <K, V> List<Cache<K, V>> getCacheList(String key, Class<K> keyClazz, Class<V> valueClazz) {
        String cacheUses = cacheUseConfig.get(key);
        List<Cache<K, V>> cacheList = new ArrayList<>();
        if (StringUtils.isEmpty(cacheUses)) {
            for (CacheLevel cacheLevel : CacheLevel.values()) {
                CacheManager cacheManager = cacheManagerMap.get(cacheLevel);
                if (null != cacheManager) {
                    cacheList.add(cacheManager.get(key, keyClazz, valueClazz));
                }
            }
            return cacheList;
        }

        List<String> levelNameList = Arrays.stream(cacheUses.split(","))
                .filter(StringUtils::isNotEmpty)
                .sorted(Comparator.comparing(String::toUpperCase))
                .collect(Collectors.toList());

        for (String levelName : levelNameList) {
            CacheLevel cacheLevel = CacheLevel.getByName(levelName);
            if (null != cacheLevel) {
                CacheManager cacheManager = cacheManagerMap.get(cacheLevel);
                if (null != cacheManager) {
                    cacheList.add(cacheManager.get(key, keyClazz, valueClazz));
                }
            }
        }
        return cacheList;
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
