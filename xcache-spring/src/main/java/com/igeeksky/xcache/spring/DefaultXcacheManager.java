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

import java.util.List;

/**
 * <b>LocalCacheManager</b><br>
 * create and manager all local cache
 *
 * @author Tony.Lau
 * @link https://my.oschina.net/xcafe
 * @date 2017-02-21 18:39:12
 */
public class DefaultXcacheManager extends AbstractXcacheManager {

    private final int backSourceSize;

    private final long expiration;

    private final long maxIdleTime;

    private int singleStoreMaxElements;

    private volatile boolean dynamic = true;

    /**
     * @param expiration             element time to remove(TimeUnit.SECONDS)
     * @param singleStoreMaxElements max elements in cache
     * @param cacheCleanPeriod       time to excute cache cleaner(TimeUnit.SECONDS)
     */
    public DefaultXcacheManager(long expiration, long maxIdleTime, int backSourceSize, int singleStoreMaxElements, int cacheCleanPeriod) {
        this.expiration = expiration;
        this.maxIdleTime = maxIdleTime;
        this.backSourceSize = backSourceSize;
        this.singleStoreMaxElements = singleStoreMaxElements;
    }

    public long getExpiration() {
        return expiration;
    }

    public int getSingleStoreMaxElements() {
        return singleStoreMaxElements;
    }

    public boolean isAllowNullValues() {
        return false;
    }

    @Override
    protected DefaultXcache createCache(String name) {
        return null;
    }

    @Override
    public List<Cache> getAllCache() {
        return null;
    }
}
