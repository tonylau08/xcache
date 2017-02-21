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
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

/**
 * <b>LocalCacheManager</b><br>
 * create and manager all local cache
 * @author Tony.Lau
 * @blog: https://my.oschina.net/xcafe
 * @createTime 2017-02-21 18:39:12
 */
public class XlocalCacheManager implements CacheManager {
	
	private ConcurrentHashMap<String, Cache> cacheMap = new ConcurrentHashMap<String, Cache>(16);
	
	/** second */
	private long aliveTime;
	
	private int singleStoreMaxElements;
	
	private boolean dynamic = true;
	
	private final ScheduledExecutorService executor;
	
	private final XcacheManagerCleanner cleaner;
	
	/**
	 * @param aliveTime element time to remove(TimeUnit.SECONDS)
	 * @param singleStoreMaxElements  max elements in cache
	 * @param cacheCleanPeriod  time to excute cache cleaner(TimeUnit.SECONDS)
	 */
	public XlocalCacheManager(long aliveTime, int singleStoreMaxElements, int cacheCleanPeriod) {
		this.aliveTime = aliveTime;
		this.singleStoreMaxElements = singleStoreMaxElements;
		this.executor = Executors.newScheduledThreadPool(1);
		this.cleaner = new XcacheManagerCleanner(this);
		this.executor.scheduleWithFixedDelay(cleaner, 20, cacheCleanPeriod, TimeUnit.SECONDS);
	}
	
	public void setInitCaches(Set<XlocalCacheConfig> initCaches){
		Iterator<XlocalCacheConfig> it = initCaches.iterator();
		while(it.hasNext()){
			XlocalCacheConfig args = it.next();
			System.out.println(args);
			String name = args.getName();
			Cache cache = this.cacheMap.get(name);
			if (cache == null && dynamic) {
				synchronized (this.cacheMap) {
					cache = this.cacheMap.get(name);
					if (cache == null) {
						this.cacheMap.put(name, createCache(args));
					}
				}
			}
		}
	}
	
	public long getAliveTime() {
		return aliveTime;
	}

	public int getSingleStoreMaxElements() {
		return singleStoreMaxElements;
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
	
	public List<Cache> getAllCache() {
		return Collections.list(cacheMap.elements());
	}

	@Override
	public Collection<String> getCacheNames() {
		return Collections.unmodifiableSet(this.cacheMap.keySet());
	}
	
	public void setCacheNames(Collection<String> cacheNames) {
		if (cacheNames != null) {
			for (String name : cacheNames) {
				getCache(name);
			}
			this.dynamic = false;
		}
		else {
			this.dynamic = true;
		}
	}
	
	public boolean isAllowNullValues() {
		return false;
	}
	
	private XlocalCache createCache(String name) {
		return new XlocalCache(name, aliveTime, singleStoreMaxElements, executor);
	}
	
	private XlocalCache createCache(XlocalCacheConfig args) {
		String name = args.getName();
		long alive = args.getAliveTime() == null ? aliveTime : args.getAliveTime();
		int maxElements = args.getSingleStoreMaxElements() == null ? singleStoreMaxElements : args.getSingleStoreMaxElements();
		return new XlocalCache(name, alive, maxElements, executor);
	}
	

}
