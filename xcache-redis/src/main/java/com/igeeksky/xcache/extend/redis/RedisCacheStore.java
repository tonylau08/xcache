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

package com.igeeksky.xcache.extend.redis;


import com.igeeksky.xcache.core.AbstractCacheStore;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

/**
 * @author Tony.Lau
 * @blog https://my.oschina.net/xcafe
 * @date 2017-02-22 16:17:03
 */
public class RedisCacheStore extends AbstractCacheStore {

    private static final Charset CHARSET = StandardCharsets.UTF_8;

    public RedisCacheStore(String name, long expireAfterWrite) {
        super(name, expireAfterWrite);
    }

    @Override
    protected ValueWrapper fromStore(Object o) {
        return null;
    }

    @Override
    public CompletableFuture<ValueWrapper> asyncGet(Object o) {
        return null;
    }

    @Override
    public CompletableFuture asyncGet(Object o, Class aClass) {
        return null;
    }

    @Override
    public void put(Object o, Object o2) {

    }

    @Override
    public ValueWrapper putIfAbsent(Object o, Object o2) {
        return null;
    }

    @Override
    public void evict(Object o) {

    }

    @Override
    public void clear() {

    }

}
