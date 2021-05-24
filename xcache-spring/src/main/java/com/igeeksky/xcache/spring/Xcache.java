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

import com.igeeksky.xcache.core.KeyValue;
import org.springframework.cache.Cache;

import java.util.Collection;

/**
 * @author Tony.Lau
 * @blog: https://my.oschina.net/xcafe
 * @createTime 2017-02-27 19:18:03
 */
public interface Xcache extends Cache {

    /**
     * 批量保存数据到缓存
     *
     * @param keyValues
     * @param <K>
     * @param <V>
     */
    <K, V> void putList(Collection<KeyValue<K, V>> keyValues);

    /**
     * 批量保存数据到缓存
     *
     * @param values
     * @param keyField
     * @param keyType
     * @param <K>
     * @param <V>
     */
    <K, V> void putList(Collection<V> values, String keyField, Class<K> keyType);

    /**
     * 批量获取缓存数据
     *
     * @param keys
     * @param keyType
     * @param valueType
     * @param <K>
     * @param <V>
     * @return
     */
    <K, V> CacheListResult<K, V> getList(Collection<K> keys, Class<K> keyType, Class<V> valueType);

    /**
     * 批量获取缓存数据，未命中缓存的执行valueLoader
     *
     * @param keys
     * @param keyType
     * @param valueType
     * @param valueLoader
     * @param <K>
     * @param <V>
     * @return
     */
    <K, V> CacheListResult<K, V> getList(Collection<K> keys, Class<K> keyType, Class<V> valueType,
                                         ListValueLoader<K, V> valueLoader);

    default org.springframework.cache.Cache.ValueWrapper convert(com.igeeksky.xcache.core.CacheStore.ValueWrapper wrapper) {
        return null == wrapper ? null : () -> wrapper.get();
    }

}
