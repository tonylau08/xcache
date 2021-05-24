/*
 * Copyright 2017-2020 the original author or authors.
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
 * @author Patrick.Lau
 * @date 2020-12-10
 */
public abstract class AbstractXcacheManager implements XcacheManager {

    protected final ConcurrentHashMap<String, Xcache> cacheMap = new ConcurrentHashMap<>();

    private boolean dynamic = true;

    @Override
    public Xcache getXcache(String name) {
        Xcache cache = this.cacheMap.get(name);
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
    public Cache getCache(String name) {
        return getXcache(name);
    }

    protected abstract Xcache createCache(String name);

    @Override
    public Collection<String> getCacheNames() {
        return Collections.unmodifiableSet(this.cacheMap.keySet());
    }

    public abstract List<Cache> getAllCache();
}
