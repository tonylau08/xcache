package com.igeeksky.xcache.core;

class XLocalCacheCleanner implements Runnable {

	private XlocalCache localCache;

	public XLocalCacheCleanner(XlocalCache localCache) {
		this.localCache = localCache;
	}

	@Override
	public void run() {
		localCache.clean();
	}

}