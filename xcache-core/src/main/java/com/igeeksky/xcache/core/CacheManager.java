package com.igeeksky.xcache.core;

import java.util.Collection;

/**
 * @author Patrick.Lau
 * @date 2021-06-10
 */
public interface CacheManager {

    Cache get(String name);

    Collection<Cache> getAll();

    Collection<String> getAllCacheNames();

}
