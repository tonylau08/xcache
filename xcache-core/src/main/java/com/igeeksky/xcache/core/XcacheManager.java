/*
 * Copyright 2017 Tony.lau All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.igeeksky.xcache.core;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.core.RedisOperations;

/**
 * @author Tony.Lau
 * @blog: https://my.oschina.net/xcafe
 * @createTime 2017-02-21 18:38:02
 */
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
