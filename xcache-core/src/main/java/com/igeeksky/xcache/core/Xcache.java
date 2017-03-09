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

package com.igeeksky.xcache.core;

import java.util.List;

import org.springframework.cache.Cache;

/**
 * @author Tony.Lau
 * @blog: https://my.oschina.net/xcafe
 * @createTime 2017-02-27 16:18:03
 */
public interface Xcache extends Cache {

	public <T> void putList(Object key, Object[] values, String idField, Class<T> idType);

	public void putRelation(Object key, Object value, String idField);

	public ValueWrapper getRelation(Object key);

	public <T> T getRelation(Object key, Class<T> type);

	public Object getRelationId(Object key);

	public void putOther(Object key, int second, Object value);

	public ValueWrapper getOther(Object key);

	public <T> T getOther(Object key, Class<T> type);

	public void delOther(Object key);

	public ValueWrapper getIds(Object key);

	public <T>T getRelationId(Object key, Class<T> idType);

	public <T>List<T> getIds(Object key, Class<T> idType);

	public <E, T> CacheListResult<E, T> getList(Object key, Class<E> idType, Class<T> valueType);


}
