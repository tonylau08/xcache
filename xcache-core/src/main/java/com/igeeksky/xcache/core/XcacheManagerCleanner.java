package com.igeeksky.xcache.core;

import java.util.List;

import org.springframework.cache.Cache;

public class XcacheManagerCleanner implements Runnable {

	private XlocalCacheManager cacheManager;

	public XcacheManagerCleanner(XlocalCacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	@Override
	public void run() {
		List<Cache> caches = cacheManager.getAllCache();
		for (Cache cache : caches) {
			((XlocalCache) cache).clean();
		}
	}

}