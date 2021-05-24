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

import com.igeeksky.xcache.core.*;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

/**
 * @author Patrick.Lau
 * @createTime 2017-02-21 18:23:40
 */
public class CompositeCache extends AbstractXcache {

    private Xcache firstCache;

    private Xcache secondCache;

    CompositeCache(String name, int backSourceSize, Xcache firstCache, Xcache secondCache) {
        super(name, backSourceSize);
        this.firstCache = firstCache;
        this.secondCache = secondCache;
    }

    @Override
    public Object getNativeCache() {
        return null;
    }

    @Override
    public ValueWrapper get(Object key) {
        return null;
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        return null;
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        return null;
    }

    @Override
    public void put(Object key, Object value) {

    }

    @Override
    public void evict(Object key) {

    }

    @Override
    public void clear() {

    }

    @Override
    public CompletableFuture<CacheStore.ValueWrapper> asyncGet(Object key) {
        return null;
    }
}
