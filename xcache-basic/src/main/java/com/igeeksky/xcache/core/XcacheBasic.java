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

import com.igeeksky.xcache.support.CacheKey;

/**
 * @author Tony.Lau
 * @createTime 2017-02-21 18:23:40
 */
public class XcacheBasic implements Cache {
	
	private CacheKey cacheKey;
	
	private RedisOperations<String, Long> redisOperations;
	
	private Cache remoteCache;
	
	private Cache localCache;
	
	private final String name;
	
	private final String versionDelimiter = ":_ver";
	
	private final String VERSION_KEY;
	
	XcacheBasic(String name, Cache RemoteCache, Cache localCache, RedisOperations<String, Long> redisOperations){
		this.name = name;
		this.VERSION_KEY = name + versionDelimiter;
		this.remoteCache = (RemoteCache)RemoteCache;
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
		LocalCacheElement localCacheElement = localCache.get(key, LocalCacheElement.class);
		
		// 2.如果本地缓存为空，获取远程缓存保存到本地并返回值
		if(null == localCacheElement){
			ValueWrapper wrapper = remoteCache.get(key);
			if(null != wrapper){
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
				localCacheElement = new LocalCacheElement(remoteVersion, wrapper);
				localCache.put(key, localCacheElement);
			}
			return wrapper;
		}
		
		// 3.本地缓存不为空：比较远程缓存版本
		Long localVersion = localCacheElement.getVersion();
		Object remoteVersionObj = redisOperations.opsForHash().get(VERSION_KEY, key);
		if(null == remoteVersionObj){
			localCache.evict(key);
			return null;
		}
		
		// 4.版本相同：返回本地缓存
		Long remoteVersion = Long.valueOf(remoteVersionObj.toString());
		if(localVersion.equals(remoteVersion)){
			return (ValueWrapper) localCacheElement.get();
		}
		
		
		// 5.版本不同：返回远程缓存
		ValueWrapper wrapper = remoteCache.get(key);
		if(null != wrapper){
			localCacheElement.setVersion(remoteVersion).setObj(wrapper);
			localCache.put(key, localCacheElement);
		}
		return wrapper;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(Object key, Class<T> type) {
		ValueWrapper wrapper = get(key);
		return wrapper == null ? null : (T) wrapper.get();
	}

	/**
	 * <b>注意事项：</b><br>
	 * 1. 因为递增版本和保存数据到远程并非原子操作，所以可能出现两个问题：<br>
	 *   1.1 如果先保存数据，未递增版本号之前：远程版本号与本地版本号一致，导致返回本地缓存旧数据。<br>
	 * 	 1.2 如果先递增版本号，未保存数据到远程服务器之前：本地缓存从远程服务器读取到旧数据，但版本号却已经与远程服务器已经一致。<br>
	 *   第一种方式程序可以自动纠正，且通常两条命令间隔非常小；第二种方式只有重启程序才能纠正，因此采用第一种方式。<br>
	 *   
	 * 2. 如果采用Lua脚本进行原子操作，那么Redis Server需非集群模式。如果是集群，版本key 与 数据Key 通常不可能在同一服务器。<br>
	 * 2.1 如果是集群要采用Lua脚本，那么 数据和版本 只能使用同一个hashKey，
	 * 而且Redis客户端实现必须支持执行脚本时支持通过hashKey找到服务器，遗憾的是当前还没有客户端支持，虽然Redis集群规范中支持使用hash tags来将多个key强制存入同一个slot。
	 * 即使可以hash到同一个slot，但如果有一张活跃的超级大表，那么就很有可能导致同一台机器负荷过重。
	 * 2.2 这又会导致另外一个问题，缓存无法实现自动过期，只能增加过期时间然后以定时任务的方式来清理。
	 * 3. 这些问题Redis无法解决，只能从业务层面入手，通过id分片机制主动分配到不同的服务器，但无疑会大大增加复杂度，特别是分页查询逻辑。
	 */
	@Override
	public void put(Object key, Object value) {
		//Long remoteVersion = remoteCache.putAsVersion(key, value);
		
		// 1.保存到远程组件缓存
		remoteCache.put(key, value);
		
		// 2.判断远程缓存保存是否成功
		ValueWrapper wrapper = remoteCache.get(key);
		if(null == wrapper){
			return;
		}
		
		// 3.保存版本号
		Long remoteVersion = redisOperations.opsForHash().increment(VERSION_KEY, key, 1l);
		
		// 3.保存本地组件缓存
		if(null != remoteVersion){
			LocalCacheElement localCacheElement = new LocalCacheElement(remoteVersion, wrapper);
			localCache.put(key, localCacheElement);
		}
	}

	@Override
	public ValueWrapper putIfAbsent(Object key, Object value) {
		// 1.清理本地缓存
		localCache.evict(key);
		
		// 2.保存到远程组件缓存
		return remoteCache.putIfAbsent(key, value);
	}

	@Override
	public void evict(Object key) {
		// 1.删除远程缓存
		remoteCache.evict(key);
		
		// 2.删除本地缓存及版本号
		localCache.evict(key);
	}

	@Override
	public void clear() {
		redisOperations.delete(VERSION_KEY);
		remoteCache.clear();
		localCache.clear();
	}

}
