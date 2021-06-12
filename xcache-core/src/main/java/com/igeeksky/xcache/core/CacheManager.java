package com.igeeksky.xcache.core;

import java.util.Collection;

/**
 * @author Patrick.Lau
 * @date 2021-06-10
 */
public interface CacheManager {

    XCache get(String name);

    Collection<XCache> getAll();

    Collection<String> getAllCacheNames();

}
