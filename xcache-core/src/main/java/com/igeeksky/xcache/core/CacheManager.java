package com.igeeksky.xcache.core;

import java.util.Collection;

/**
 * @author Patrick.Lau
 * @date 2021-06-10
 */
public interface CacheManager {

    Cache<Object, Object> get(String name);

    <K, V> Cache<K, V> get(String name, Class<K> keyClazz, Class<V> valueClazz);

    Collection<Cache> getAll();

    Collection<String> getAllCacheNames();

}
