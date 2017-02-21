package com.igeeksky.xcache.core;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.core.RedisOperations;

public class XcacheManager implements CacheManager {
	
	private ConcurrentHashMap<String, Cache> cacheMap = new ConcurrentHashMap<String, Cache>(16);
	
	private RedisCacheManager remoteCacheManager;
	
	private XlocalCacheManager localCacheManager;
	
	private RedisOperations<String ,Long> redisOperations;
	
	private boolean dynamic = true;
	
	public XcacheManager(RedisCacheManager remoteCacheManager, XlocalCacheManager localCacheManager, RedisOperations<String ,Long> redisOperations) {
		this.remoteCacheManager = remoteCacheManager;
		this.localCacheManager = localCacheManager;
		this.redisOperations = redisOperations;
	}

	@Override
	public Cache getCache(String name) {
		Cache cache = this.cacheMap.get(name);
		if (cache == null && this.dynamic) {
			synchronized (this.cacheMap) {
				cache = this.cacheMap.get(name);
				if (cache == null) {
					cache = createCache(name);
					this.cacheMap.put(name, cache);
				}
			}
		}
		return cache;
	}

	@Override
	public Collection<String> getCacheNames() {
		return Collections.unmodifiableSet(this.cacheMap.keySet());
	}
	
	public boolean isAllowNullValues() {
		return false;
	}
	
	private Xcache createCache(String name) {
		return new Xcache(name, remoteCacheManager.getCache(name), localCacheManager.getCache(name), redisOperations);
	}

}
