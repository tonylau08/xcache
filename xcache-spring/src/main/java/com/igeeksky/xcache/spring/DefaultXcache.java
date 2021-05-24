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

import com.igeeksky.xcache.core.CacheStore;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

/**
 * @author Patrick.Lau
 * @blog https://my.oschina.net/xcafe
 * @date 2017-02-21 18:38:43
 */
public class DefaultXcache extends AbstractXcache {

    private final long expiration;
    private final boolean nullable = true;
    private final CacheStore cacheStore;

    public DefaultXcache(String name, int backSourceSize, long expiration, CacheStore cacheStore) {
        super(name, backSourceSize);
        this.expiration = expiration;
        this.cacheStore = cacheStore;
    }

    @Override
    public Object getNativeCache() {
        return cacheStore;
    }

    @Override
    public ValueWrapper get(Object key) {
        return convert(cacheStore.get(key));
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        return cacheStore.get(key, type);
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        return get(key, valueLoader, expiration);
    }

    private <T> T get(Object key, Callable<T> valueLoader, long expireSecond) {
        Objects.requireNonNull(valueLoader, "valueLoader must not be null");

        com.igeeksky.xcache.core.CacheStore.ValueWrapper wrapper = cacheStore.get(key);
        if (wrapper != null) {
            return (T) wrapper.getValue();
        }

        T value = null;
        synchronized (lockSupport.getLockByKey(key)) {
            wrapper = cacheStore.get(key);
            if (wrapper != null) {
                return (T) wrapper.getValue();
            }

            try {
                value = valueLoader.call();
            } catch (Throwable ex) {
                throw new ValueRetrievalException(key, valueLoader, ex);
            }
        }

        if (nullable || (null != value)) {
            cacheStore.put(key, value);
        }

        return value;
    }

    @Override
    public void put(Object key, Object value) {
        if (!nullable) {
            Objects.requireNonNull(value, "value must not be null");
        }

        cacheStore.put(key, value);
    }

    /**
     * @param key   缓存Key
     * @param value 缓存Value
     * @return 1. 如果Key已存在，则不替换，并返回原值；<br>
     * 2. 如果Key不存在，则新保存，并返回空值。
     */
    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        return convert(cacheStore.putIfAbsent(key, value));
    }

    @Override
    public void evict(Object key) {
        cacheStore.evict(key);
    }

    @Override
    public void clear() {
        this.cacheStore.clear();
    }

    @Override
    public CompletableFuture<CacheStore.ValueWrapper> asyncGet(Object key) {
        return null;
    }
}
