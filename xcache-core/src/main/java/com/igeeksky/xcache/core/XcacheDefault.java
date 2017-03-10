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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.cache.Cache;
import org.springframework.util.Assert;

import com.R;
import com.igeeksky.xcache.support.CacheKey;
import com.igeeksky.xcache.support.KeyValue;
import com.igeeksky.xcache.support.gen.VersionGen;
import com.igeeksky.xcache.util.BeanUtils;

/**
 * @author Tony.Lau
 * @createTime 2017-02-21 18:23:40
 */
public class XcacheDefault implements Xcache {

	private CacheKey cacheKey;

	private RemoteCache remoteCache;

	private Cache localCache;

	private final String name;

	XcacheDefault(String name, Cache RemoteCache, Cache localCache) {
		this.name = name;
		this.remoteCache = (RemoteCache) RemoteCache;
		this.localCache = localCache;
	}

	public CacheKey getCacheKey() {
		return cacheKey;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Object getNativeCache() {
		return remoteCache;
	}

	/**
	 * <B>DONE</B> <B>获取缓存元素</B><br>
	 * 缓存版本由缓存元素自行管理，过期时间由Store自行管理
	 */
	@Override
	public ValueWrapper get(Object key) {
		// 1.获取本地缓存版本
		LocalCacheElement localCacheElement = localCache.get(key, LocalCacheElement.class);
		Long localVersion = null;
		
		// 2.本地缓存为空，获取远程缓存保存到本地并返回值
		if (null == localCacheElement || (localVersion = localCacheElement.getVersion()) == null) {
			ValueWrapper wrapper = remoteCache.get(key);
			if (null != wrapper) {
				Object value = wrapper.get();
				localCacheElement = new LocalCacheElement(VersionGen.gen(value), value);
				localCache.put(key, localCacheElement);
			}
			return localCacheElement;
		}

		// 3.本地缓存不为空：比较本地版本与远程版本
		R r = remoteCache.getWithVersion(key, localVersion);
		if (null != r) {
			// 4.版本不同：r.s = 2，保存远程缓存到本地并返回；版本相同：r.s =
			// 1，返回本地缓存；其它情况，删除本地缓存，返回null
			if (r.s == 2) {
				localCacheElement = new LocalCacheElement(VersionGen.gen(r.o), r.o);
				localCache.put(key, localCacheElement);
				return localCacheElement;
			} else if (r.s == 1) {
				return localCacheElement;
			}
		}
		localCache.evict(key);
		return null;
	}

	/**
	 * <B>DONE</B> <B>根据指定类型获取缓存元素</B><br>
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(Object key, Class<T> valueType) {
		ValueWrapper wrapper = get(key);
		return wrapper == null ? null : (T) wrapper.get();
	}

	/**
	 * <B>DONE</B>
	 */
	@Override
	public void put(Object key, Object value) {
		if(null == value){
			return;
		}
		
		// 1.获取元素的版本(无需比较本地版本，以远程版本为准，否则可能因为线程执行的时间片关系导致本地版本比远程版本更新)
		Long version = VersionGen.gen(value);
		localCache.evict(key);
		
		LocalCacheElement localCacheElement = new LocalCacheElement(version, value);

		// 2.保存元素到远程缓存，并返回状态码
		int status = remoteCache.putWithVersion(key, value, version);
		
		// 3.保存元素到本地缓存
		if (status == 1 || status == 2) {
			localCache.put(key, localCacheElement);
		}
	}

	@Override
	public ValueWrapper putIfAbsent(Object key, Object value) {
		Assert.notNull(key, "Key must not be null");
		Assert.notNull(value, "value must not be null");
		
		// 1.清除本地缓存
		localCache.evict(key);
		
		Long version = VersionGen.gen(value);
		Assert.notNull(version, "entity version field is required,it must not be null");

		// 2.保存到远程组件缓存（为避免远程缓存和本地缓存不一致，无论是否成功均清除本地缓存）
		return remoteCache.putIfAbsent(key, value);
	}

	@Override
	public void evict(Object key) {
		Assert.notNull(key, "Key must not be null");
		// 1.删除本地缓存及版本号
		localCache.evict(key);
		// 2.删除远程缓存及key集合中的元素
		remoteCache.evict(key);
	}

	@Override
	public void clear() {
		System.out.println("xcache default clear");
		remoteCache.clear();
		localCache.clear();
	}

	@Override
	public void putRelation(Object key, Object element, String idField) {
		Assert.notNull(key, "Key must not be null");
		Assert.notNull(idField, "idField must not be null");
		
		if(null == element){
			return;
		}
		
		Object id = BeanUtils.getBeanProperty(element, idField);
		if (null != id) {
			remoteCache.putRelation(key, id);
		}
	}

	@Override
	public ValueWrapper getRelation(Object key) {
		ValueWrapper idWrapper = getRelationId(key);
		if (null != idWrapper) {
			return get(idWrapper.get());
		}
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getRelation(Object key, Class<T> valueType) {
		Assert.notNull(valueType, "valueType must not be null");
		ValueWrapper wrapper = getRelation(key);
		return wrapper == null ? null : (T) wrapper.get();
	}

	@Override
	public ValueWrapper getRelationId(Object key) {
		Assert.notNull(key, "Key must not be null");
		return remoteCache.getRelationId(key);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getRelationId(Object key, Class<T> idType) {
		Assert.notNull(idType, "idType must not be null");
		ValueWrapper wrapper = getRelationId(key);
		return wrapper == null ? null : (T) wrapper.get();
	}

	@Override
	public ValueWrapper getIds(Object key) {
		Assert.notNull(key, "Key must not be null");
		ValueWrapper wrapper = remoteCache.getIds(key);
		return wrapper;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> List<T>  getIds(Object key, Class<T> idType) {
		Assert.notNull(idType, "idType must not be null");
		ValueWrapper wrapper = getIds(key);
		return wrapper == null ? null : (List<T>) wrapper.get();
	}

	@Override
	public <T> void putList(Object key, Object[] elements, String idField, Class<T> idType) {
		if(null == elements || elements.length == 0){
			return;
		}
		Assert.notNull(key, "Key must not be null");
		Assert.notNull(idField, "idField must not be null");
		List<T> ids = BeanUtils.getBeansProperty(elements, idField, idType);
		remoteCache.putList(key, ids);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <E, T> CacheListResult<E, T> getList(Object key, Class<E> idType, Class<T> valueType) {
		Assert.notNull(idType, "idType must not be null");
		Assert.notNull(valueType, "valueType must not be null");
		Assert.notNull(key, "Key must not be null");
		
		List<E> ids = getIds(key, idType);
		
		int size;
		if(null == ids || (size = ids.size()) == 0){
			return null;
		}

		KeyValue[] kvs = new KeyValue[size];
		for (int i = 0; i < size; i++) {
			E id = ids.get(i);
			LocalCacheElement localCacheElement = localCache.get(id, LocalCacheElement.class);
			if (null != localCacheElement) {
				kvs[i] = new KeyValue(id, localCacheElement.getVersion());
			} else {
				kvs[i] = new KeyValue(id, null);
			}
		}
		
		Map<Object, R> remoteR = remoteCache.getListCmpVersion(kvs);
		Map<E, T> elements = new HashMap<E, T>();
		List<E> nonCacheIds = new ArrayList<E>();
		for (int i = 0; i < size; i++) {
			E id = ids.get(i);
			R r = remoteR.get(id);
			if (null == r) {
				nonCacheIds.add(id);
				localCache.evict(id);
				continue;
			}

			Integer status = r.s;
			if (null == status) {
				nonCacheIds.add(id);
				localCache.evict(id);
				continue;
			}

			if (status == 0) {
				localCache.evict(id);
				nonCacheIds.add(id);
				continue;
			} else if (status == 1) {
				LocalCacheElement localCacheElement = localCache.get(id, LocalCacheElement.class);
				if (null != localCacheElement) {
					elements.put(id, (T)localCacheElement.get());
				} else {
					nonCacheIds.add(id);
				}
				continue;
			} else if (status == 2) {
				LocalCacheElement localCacheElement = localCache.get(key, LocalCacheElement.class);
				if (null != localCacheElement) {
					Long localVersion = localCacheElement.getVersion();
					if (r.v > localVersion) {
						localCache.put(id, localCacheElement.setVersion(r.v).setObj(r.o));
						elements.put(id, (T)r.o);
					} else if (r.v == localVersion) {
						elements.put(id, (T)r.o);
					} else {
						localCache.evict(id);
						nonCacheIds.add(id);
					}
				} else {
					nonCacheIds.add(id);
				}
				continue;
			} else if (status == 4) {
				if (localCache.get(id) == null) {
					localCache.put(id, new LocalCacheElement(VersionGen.gen(r.o), r.o));
					elements.put(id, (T)r.o);
				} else {
					localCache.evict(id);
					nonCacheIds.add(id);
				}
				continue;
			} else {
				localCache.evict(id);
				nonCacheIds.add(id);
				continue;
			}
		}

		CacheListResult<E, T> result = new CacheListResult<E, T>();
		result.setAllIds(ids);
		result.setCacheElements(elements);
		result.setNonCacheIds(nonCacheIds);
		return result;
	}

	@Override
	public void putOther(Object key, int second, Object value) {
		Assert.notNull(key, "Key must not be null");
		Assert.notNull(value, "value must not be null");
		
		remoteCache.putOther(key, second, value);
	}

	@Override
	public ValueWrapper getOther(Object key) {
		Assert.notNull(key, "Key must not be null");
		return remoteCache.getOther(key);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getOther(Object key, Class<T> type) {
		Assert.notNull(type, "Class Type must not be null");
		
		ValueWrapper wrapper = getOther(key);
		return wrapper == null ? null : (T) wrapper.get();
	}

	@Override
	public void delOther(Object key) {
		Assert.notNull(key, "Key must not be null");
		remoteCache.delOther(key);
	}

}
