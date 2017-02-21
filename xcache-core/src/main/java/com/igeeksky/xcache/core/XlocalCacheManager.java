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

public class XlocalCacheManager implements CacheManager {
	
	private ConcurrentHashMap<String, Cache> cacheMap = new ConcurrentHashMap<String, Cache>(16);
	
	private long aliveTime = 3600;
	
	private int singleStoreMaxElements = 1024;
	
	private boolean dynamic = true;
	
	private final ScheduledExecutorService executor;
	
	private final XcacheManagerCleanner cleaner;
	
	public XlocalCacheManager(long aliveTime, int singleStoreMaxElements) {
		this.aliveTime = aliveTime;
		this.singleStoreMaxElements = singleStoreMaxElements;
		this.executor = Executors.newScheduledThreadPool(1);
		this.cleaner = new XcacheManagerCleanner(this);
		this.executor.scheduleWithFixedDelay(cleaner, 20, 600, TimeUnit.SECONDS);
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

	public void setAliveTime(long aliveTime) {
		this.aliveTime = aliveTime;
	}

	public int getSingleStoreMaxElements() {
		return singleStoreMaxElements;
	}

	public void setSingleStoreMaxElements(int singleStoreMaxElements) {
		this.singleStoreMaxElements = singleStoreMaxElements;
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
