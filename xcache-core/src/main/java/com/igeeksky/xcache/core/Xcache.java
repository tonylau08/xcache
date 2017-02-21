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

import org.springframework.cache.Cache;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.util.Assert;

import com.igeeksky.xcache.support.CacheKey;

/**
 * @author Tony.Lau
 * @createTime 2017-02-21 18:23:40
 */
public class Xcache implements Cache {
	
	private CacheKey cacheKey;
	
	private RedisOperations<String, Long> redisOperations;
	
	private Cache remoteCache;
	
	private Cache localCache;
	
	private final String name;
	
	private final String versionDelimiter = ":_ver";
	
	private final String VERSION_KEY;
	
	Xcache(String name, Cache remoteCache, Cache localCache, RedisOperations<String, Long> redisOperations){
		this.name = name;
		this.VERSION_KEY = name + versionDelimiter;
		this.remoteCache = remoteCache;
		this.localCache = localCache;
		this.redisOperations = redisOperations;
	}
	
	public CacheKey getCacheKey(){
		return cacheKey;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Object getNativeCache() {
		return redisOperations;
	}

	@Override
	public ValueWrapper get(Object key) {
		// 1.获取本地缓存版本
		CacheElement localCacheElement = localCache.get(key, CacheElement.class);
		System.out.println("GET:1");
		
		// 2.如果本地缓存为空，获取远程缓存保存到本地并返回值
		if(null == localCacheElement){
			System.out.println("GET:2");
			ValueWrapper wrapper = remoteCache.get(key);
			if(null != wrapper){
				System.out.println("GET:3");
				Object remoteVersionObj = redisOperations.opsForHash().get(VERSION_KEY, key);
				Long remoteVersion;
				if(null == remoteVersionObj){
					remoteVersion = redisOperations.opsForHash().increment(VERSION_KEY, key, 1l);
					if(null == remoteVersion){
						return wrapper;
					}
				}else{
					remoteVersion = Long.valueOf(remoteVersionObj.toString());
				}
				localCacheElement = new CacheElement(remoteVersion, wrapper);
				localCache.put(key, localCacheElement);
			}
			return wrapper;
		}
		
		// 3.本地缓存不为空：比较远程缓存版本
		Long localVersion = localCacheElement.getVersion();
		Object remoteVersionObj = redisOperations.opsForHash().get(VERSION_KEY, key);
		System.out.println("GET:4");
		if(null == remoteVersionObj){
			return null;
		}
		
		// 4.版本相同：返回本地缓存
		Long remoteVersion = Long.valueOf(remoteVersionObj.toString());
		System.out.println("GET:5");
		if(localVersion.equals(remoteVersion)){
			return localCacheElement.getValueWrapper();
		}
		
		
		// 5.版本不同：返回远程缓存
		ValueWrapper wrapper = remoteCache.get(key);
		System.out.println("GET:6");
		if(null != wrapper){
			localCacheElement.setVersion(remoteVersion).setValueWrapper(wrapper);
			localCache.put(key, localCacheElement);
		}
		System.out.println("GET:7");
		return wrapper;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(Object key, Class<T> type) {
		ValueWrapper wrapper = get(key);
		return wrapper == null ? null : (T) wrapper.get();
	}

	@Override
	public void put(Object key, Object value) {
		// 1.保存到远程组件缓存
		remoteCache.put(key, value);
		
		// 2.判断远程缓存保存是否成功
		ValueWrapper wrapper = remoteCache.get(key);
		if(null == wrapper){
			return;
		}
		
		// 2.保存版本号
		Long remoteVersion = redisOperations.opsForHash().increment(VERSION_KEY, key, 1l);
		
		// 3.保存本地组件缓存
		if(null != remoteVersion){
			CacheElement localCacheElement = new CacheElement(remoteVersion, wrapper);
			localCache.put(key, localCacheElement);
		}
	}

	@Override
	public ValueWrapper putIfAbsent(Object key, Object value) {
		// 1.保存到远程组件缓存
		ValueWrapper wrapper = remoteCache.putIfAbsent(key, value);
		
		// 2.如果返回值为空，说明远程缓存Key不存在，并且保存成功，同步更新本地缓存
		if(null == wrapper){
			
			wrapper = remoteCache.get(key);
			Long remoteVersion = redisOperations.opsForHash().increment(VERSION_KEY, key, 1l);
			
			// 3.保存本地组件缓存
			if(null != wrapper && null != remoteVersion){
				CacheElement localCacheElement = new CacheElement(remoteVersion, wrapper);
				localCache.put(key, localCacheElement);
			}
			return null;
		}
		
		return wrapper;
	}

	@Override
	public void evict(Object key) {
		// 1.删除本地缓存及版本号
		localCache.evict(key);
		
		// 2.删除远程缓存
		remoteCache.evict(key);
		
		// 3.删除远程缓存版本号
		redisOperations.opsForHash().delete(VERSION_KEY, key);
		
	}

	@Override
	public void clear() {
		localCache.clear();
		remoteCache.clear();
		redisOperations.delete(VERSION_KEY);
	}
	
	private static class CacheElement{
		
		private Long version;
		
		private ValueWrapper valueWrapper;
		
		public CacheElement(Long version, ValueWrapper valueWrapper) {
			Assert.notNull(version, "version must not be null");
			Assert.notNull(valueWrapper, "valueWrapper must not be null");
			this.version = version;
			this.valueWrapper = valueWrapper;
		}

		public long getVersion() {
			return version;
		}

		public CacheElement setVersion(Long version) {
			Assert.notNull(version, "version must not be null");
			this.version = version;
			return this;
		}

		public ValueWrapper getValueWrapper() {
			return valueWrapper;
		}

		public void setValueWrapper(ValueWrapper valueWrapper) {
			Assert.notNull(valueWrapper, "valueWrapper must not be null");
			this.valueWrapper = valueWrapper;
		}

	}

}
