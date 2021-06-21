package com.igeeksky.xcache.core;

/**
 * @author Patrick.Lau
 * @date 2021-06-21
 */
public class XCache {

    private CacheManager firstCacheManager;
    private CacheManager secondCacheManager;

    public XCache firstCacheManager(CacheManager firstCacheManager) {
        this.firstCacheManager = firstCacheManager;
        return this;
    }

    public XCache secondCacheManager(CacheManager secondCacheManager) {
        this.secondCacheManager = secondCacheManager;
        return this;
    }

    public CompositeCacheManager build() {
        return new CompositeCacheManager(firstCacheManager, secondCacheManager);
    }

    public static XCache newBuilder() {
        return new XCache();
    }

}
