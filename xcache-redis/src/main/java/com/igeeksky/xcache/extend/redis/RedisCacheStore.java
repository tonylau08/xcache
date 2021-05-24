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

import com.igeeksky.xcache.core.*;
import com.igeeksky.xcache.core.support.NullValue;
import com.igeeksky.xcache.core.support.Serializer;
import com.igeeksky.xcache.core.util.CollectionUtils;
import redis.clients.jedis.Jedis;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Tony.Lau
 * @blog https://my.oschina.net/xcafe
 * @date 2017-02-22 16:17:03
 */
public class RedisCacheStore<K,V> extends AbstractCacheStore<K,V> {

    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private final Serializer<K> keySerializer;
    private final Serializer<V> valueSerializer;
    private final RedisKeyMetadata cacheMetadata;

    private final int expiration;

    private RedisOperator redisOperator;

    private Jedis jedis;

    public RedisCacheStore(String name, int expiration, Serializer<K> keySerializer, Serializer<V> valueSerializer) {
        super(name, expiration);
        this.expiration = expiration;
        this.keySerializer = keySerializer;
        this.valueSerializer = valueSerializer;
        this.cacheMetadata = new RedisKeyMetadata(name, CHARSET, keySerializer);
    }

    protected ValueWrapper<V> toStore(K key, V value, int expiration, StoreMethod putIfAbsent) {
        Objects.requireNonNull(key, "Cache key must not be null");

        byte[] keyBytes = cacheMetadata.getKeyFullBytes(key);
        byte[] valueBytes = valueSerializer.serialize(value);
        redisOperator.setex(keyBytes, expiration, valueBytes);
        return null;
    }

    @Override
    public void put(K key, V value) {

    }

    @Override
    public void putAll(Collection<KeyValue<K, V>> keyValues) {
        if (CollectionUtils.isEmpty(keyValues)) {
            return;
        }

        List keyValueList = keyValues.stream()
                .filter(kv -> (null != kv && null != kv.key))
                .map(kv -> new KeyValue(cacheMetadata.getKeyFullBytes(kv.key), valueSerializer.serialize(kv.value)))
                .collect(Collectors.toList());

        redisOperator.mset(keyValueList, expiration);
    }

    @Override
    public CacheListResult<K, V> getAll(Collection<K> keys, Class<K> keyType, Class<V> valueType) {
        if (CollectionUtils.isEmpty(keys)) {
            return CacheListResult.emptyResult();
        }

        CacheListResult<K, V> result = CacheListResult.emptyResult();
        keys.stream().filter(Objects::nonNull).forEach(result::addNonCacheKey);

        LinkedHashSet<K> nonCacheKeys = result.getNonCacheKeys();
        int size = nonCacheKeys.size();
        if (size == 0) {
            return result;
        }

        byte[][] keysArray = new byte[size][];
        int i = 0;
        for (K key : nonCacheKeys) {
            byte[] keyBytes = cacheMetadata.getKeyFullBytes(key);
            keysArray[i] = keyBytes;
            i++;
        }

        List<byte[]> redisResultList = redisOperator.mget(keysArray);
        if (CollectionUtils.isEmpty(redisResultList)) {
            return result;
        }

        int j = 0;
        for (K key : nonCacheKeys) {
            byte[] bytes = redisResultList.get(j);
            if (null != bytes) {
                result.addCacheElement(new KeyValue<>(key, (V) valueSerializer.deserialize(bytes)));
            }
            j++;
        }

        return result;
    }

    @Override
    public ValueWrapper<V> putIfAbsent(K key, V value) {
        return null;
    }

    @Override
    public void evict(K key) {

    }

    @Override
    public void clear() {

    }

    @Override
    protected ValueWrapper<V> fromStore(K key) {
        byte[] keyFullBytes = cacheMetadata.getKeyFullBytes(key);
        byte[] valueBytes = redisOperator.get(keyFullBytes);
        return (null == valueBytes) ? null : toValueWrapper(valueSerializer.deserialize(valueBytes));
    }

    protected ValueWrapper<V> toValueWrapper(Object value) {
        if (null == value) {
            return null;
        }

        return (value instanceof NullValue) ? new SimpleValueWrapper(null) : new SimpleValueWrapper(value);
    }
}
