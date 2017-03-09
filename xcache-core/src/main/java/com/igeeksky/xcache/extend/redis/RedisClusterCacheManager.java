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

package com.igeeksky.xcache.extend.redis;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import com.igeeksky.xcache.support.redis.RedisClusterClient;

/**
 * @author Tony.Lau
 * @blog: https://my.oschina.net/xcafe
 * @createTime 2017-03-08 02:49:53
 */
public class RedisClusterCacheManager implements CacheManager {

	private final ConcurrentHashMap<String, Cache> cacheMap = new ConcurrentHashMap<String, Cache>(16);
	
	private final RedisClusterClient redisClient;
	
	private final long expiration;
	
	private boolean dynamic = true;
	
	public RedisClusterCacheManager(RedisClusterClient redisClient, long expiration) {
		this.redisClient = redisClient;
		this.expiration = expiration;
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
	
	private RedisClusterCache createCache(String name) {
		return new RedisClusterCache(name, expiration, redisClient);
	}

}
