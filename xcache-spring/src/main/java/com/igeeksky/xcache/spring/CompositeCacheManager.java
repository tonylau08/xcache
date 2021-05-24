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

package com.igeeksky.xcache.spring;

import org.springframework.cache.Cache;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Tony.Lau
 * @blog: https://my.oschina.net/xcafe
 * @createTime 2017-02-21 18:38:02
 */
public class CompositeCacheManager extends AbstractXcacheManager {

    private final ConcurrentHashMap<String, Xcache> cacheMap = new ConcurrentHashMap<>(16);

    private final XcacheManager firstCacheManager;

    private final XcacheManager secondCacheManager;

    private final int backSourceSize;

    private boolean dynamic = true;

    public CompositeCacheManager(int backSourceSize, XcacheManager firstCacheManager, XcacheManager secondCacheManager) {
        this.backSourceSize = backSourceSize;
        this.firstCacheManager = firstCacheManager;
        this.secondCacheManager = secondCacheManager;
    }

    @Override
    public Collection<String> getCacheNames() {
        return Collections.unmodifiableSet(this.cacheMap.keySet());
    }

    @Override
    public List<Cache> getAllCache() {
        return null;
    }

    public boolean isAllowNullValues() {
        return false;
    }

    @Override
    public CompositeCache createCache(String name) {
        return new CompositeCache(name, backSourceSize, firstCacheManager.getXcache(name), secondCacheManager.getXcache(name));
    }

}
