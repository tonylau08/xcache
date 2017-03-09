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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.support.SimpleValueWrapper;

import com.KV;
import com.R;
import com.igeeksky.xcache.core.RemoteCache;
import com.igeeksky.xcache.support.KeyValue;
import com.igeeksky.xcache.support.redis.RedisSingleClient;
import com.igeeksky.xcache.support.serializer.FSTSerializer;
import com.igeeksky.xcache.support.serializer.Jackson2JsonSerializer;
import com.igeeksky.xcache.support.serializer.GenericJackson2JsonSerializer;
import com.igeeksky.xcache.support.serializer.KeySerializer;
import com.igeeksky.xcache.support.serializer.StringKeySerializer;
import com.igeeksky.xcache.support.serializer.ValueSerializer;
import com.igeeksky.xcache.util.BytesUtils;
import com.igeeksky.xcache.util.NumUtils;

/**
 * @author Tony.Lau
 * @blog: https://my.oschina.net/xcafe
 * @createTime 2017-02-22 16:17:03
 */
public class RedisSingleCache implements RemoteCache {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final ValueSerializer<Object, Object> valueSerializer = new GenericJackson2JsonSerializer();

	private final KeySerializer<Object, Object> keySerializer = new StringKeySerializer();

	private final ValueSerializer<Object, Object> hashValueSerializer = new FSTSerializer();
	
	private final Jackson2JsonSerializer<KV[]> jsonRedisSerializer = new Jackson2JsonSerializer<KV[]>(KV[].class);

	private final ScriptManager scriptManager;

	private final RedisCacheMetadata metadata;

	private String name;
	private long expiration = 86400;

	private RedisSingleClient redisClient;

	public RedisSingleCache(String name, long expiration, RedisSingleClient redisClient) {
		this.name = name;
		if (expiration > 0) {
			this.expiration = expiration;
		}
		this.redisClient = redisClient;
		this.scriptManager = new RedisSingleScriptManager(name, this.expiration);
		this.metadata = new RedisCacheMetadata(name, keySerializer);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Object getNativeCache() {
		return redisClient;
	}

	/**
	 * <b>本地缓存为空，获取远程缓存</b><br>
	 */
	@Override
	public ValueWrapper get(Object key) {
		byte[] bytes = redisClient.get(metadata.getFullIdKeyBytes(key));

		if (null != bytes && bytes.length > 0) {
			return toValueWrapper(valueSerializer.deserialize(bytes));
		}

		return null;
	}

	@Override
	public R getWithVersion(Object key, Long version) {
		if (null == version) {
			return null;
		}
		byte[] fullIdKeyBytes = metadata.getFullIdKeyBytes(key);
		byte[] verBytes = valueSerializer.serialize(version);
		byte[] keySetBytes = metadata.getKeySetBytes();
		byte[] idBytes = metadata.getSuffixIdBytes(fullIdKeyBytes);
		Object object = redisClient.eval(scriptManager.getGetCmpVerScript(), 2, fullIdKeyBytes, keySetBytes, verBytes,
				idBytes);
		if (null != object && object instanceof byte[]) {
			R r = valueSerializer.deserialize((byte[]) object, R.class);
			return r;
		}
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(Object key, Class<T> type) {
		ValueWrapper wrapper = get(key);
		return wrapper == null ? null : (T) wrapper.get();
	}

	@Override
	public void put(Object key, Object value) {
		byte[] fullKeyBytes = metadata.getFullIdKeyBytes(key);
		byte[] valueBytes = valueSerializer.serialize(value);
		if (valueBytes == BytesUtils.EMPTY_BYTES) {
			return;
		}
		redisClient.set(fullKeyBytes, valueSerializer.serialize(value));
	}

	/**
	 * V1，V2，V3顺序写入数据库后，因为网络延迟等因素，以V3，V2，V1的倒序写入Redis，
	 * 如果版本不一致则删除，那么最后的Redis缓存的结果是V1,所以版本不一致并不删除Key。
	 */
	@Override
	public int putWithVersion(Object key, Object value, Long version) {
		byte[] fullIdKeyBytes = metadata.getFullIdKeyBytes(key);
		byte[] valueBytes = valueSerializer.serialize(value);
		byte[] verBytes = valueSerializer.serialize(version);
		byte[] keySetBytes = metadata.getKeySetBytes();
		byte[] idBytes = metadata.getSuffixIdBytes(fullIdKeyBytes);

		Object state = redisClient.eval(scriptManager.getPutCmpVerScript(), 2, fullIdKeyBytes, keySetBytes, valueBytes,
				verBytes, idBytes);
		Integer status;
		if (null == state || null == (status = NumUtils.getInteger(state))) {
			return 0;
		}

		if (status == 5) {
			throw new NullPointerException(new String(fullIdKeyBytes) + "remote old version is null");
		}
		if (null == version) {
			throw new NullPointerException(new String(fullIdKeyBytes) + "new version is null");
		}
		return status;
	}

	@Override
	public ValueWrapper putIfAbsent(Object key, Object value) {
		byte[] fullIdKeyBytes = metadata.getFullIdKeyBytes(key);
		byte[] valueBytes = valueSerializer.serialize(value);
		byte[] keySetBytes = metadata.getKeySetBytes();
		byte[] idBytes = metadata.getSuffixIdBytes(fullIdKeyBytes);
		if (valueBytes == BytesUtils.EMPTY_BYTES) {
			return toValueWrapper(value);
		}

		Object state = redisClient.eval(scriptManager.getPutIfAbsentScript(), 2, fullIdKeyBytes, keySetBytes,
				valueBytes, idBytes);
		Integer status;
		if (null == state || null == (status = NumUtils.getInteger(state))) {
			return toValueWrapper(value);
		}
		if (status == 1) {
			return null;
		} else {
			return toValueWrapper(value);
		}
	}

	@Override
	public void evict(Object key) {
		byte[] fullIdKeyBytes = metadata.getFullIdKeyBytes(key);
		byte[] keySetBytes = metadata.getKeySetBytes();
		byte[] idBytes = metadata.getSuffixIdBytes(fullIdKeyBytes);
		redisClient.eval(scriptManager.getEvitScript(), 2, fullIdKeyBytes, keySetBytes, idBytes);
	}

	@Override
	public void clear() {
		// 获取key集合
		byte[] prefixBytes = metadata.getIdKeyPrifixBytes();
		byte[] keySetBytes = metadata.getKeySetBytes();
		byte[] listKeyBytes = metadata.getListKeyBytes();
		byte[] relKeyBytes = metadata.getRelateKeyBytes();

		Long delNum = (Long) redisClient.eval(scriptManager.getClearScript(), 3, keySetBytes, listKeyBytes, relKeyBytes,
				prefixBytes);
		if (null != delNum) {
			logger.debug("Remote Cache clear " + (delNum - 3) + "element");
		}
	}

	private ValueWrapper toValueWrapper(Object value) {
		return (value != null ? new SimpleValueWrapper(value) : null);
	}

	/** 此方法除非单独使用RemoteCache，否则无用 */
	@Override
	public ValueWrapper getRelation(Object key) {
		ValueWrapper wrapper = getRelationId(key);
		if (null == wrapper) {
			return null;
		}
		Object id = wrapper.get();
		return id == null ? null : get(id);
	}

	@Override
	public void putRelation(Object key, Object id) {
		redisClient.hset(metadata.getRelateKeyBytes(), keySerializer.serialize(key), hashValueSerializer.serialize(id));
	}

	@Override
	public ValueWrapper getRelationId(Object key) {
		byte[] bytes = redisClient.hget(metadata.getRelateKeyBytes(), keySerializer.serialize(key));
		if (null != bytes && bytes.length > 0) {
			return toValueWrapper(hashValueSerializer.deserialize(bytes));
		}
		return null;
	}

	@Override
	public <T> void putList(Object key, List<T> ids) {
		redisClient.hset(metadata.getListKeyBytes(), keySerializer.serialize(key), hashValueSerializer.serialize(ids));
	}

	@Override
	public ValueWrapper getIds(Object key) {
		byte[] bytes = redisClient.hget(metadata.getListKeyBytes(), keySerializer.serialize(key));
		if (null != bytes && bytes.length > 0) {
			return toValueWrapper(hashValueSerializer.deserialize(bytes));
		}
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Map<Object, R> getListCmpVersion(KeyValue[] kvs) {
		int length;
		if (null == kvs || (length = kvs.length) == 0) {
			return null;
		}

		// 1.记录RedisKey 与 实体ID的映射(避免Json序列化造成的对象类型转换不一致)
		Map<String, Object> keyIdMap = new HashMap<String, Object>();

		KV[] kves = new KV[length];
		// 2.拼接FullKey String
		for (int i = 0; i < length; i++) {
			KeyValue keyValue = kvs[i];
			Object id = keyValue.getId();
			String fullIdKey = new String(metadata.getFullIdKeyBytes(id));
			keyValue.setFullIdKey(fullIdKey);
			
			keyIdMap.put(fullIdKey, id);
			kves[i] = keyValue.getKVWithId();
		}
		
		kvs = null;

		// 3.获取返回结果
		Object result = redisClient.eval(scriptManager.getGetListCmpVerScript(), 1, metadata.getKeySetBytes(), jsonRedisSerializer.serialize(kves));
		
		kves = null;
		if(null == result){
			return null;
		}
		
		// 4.实体ID 与 获取缓存的结果的映射
		Map<Object, R> idRsMap = new HashMap<Object, R>();

		// 6.写入返回结果
		HashMap<String, R> rs = valueSerializer.deserialize((byte[]) result, HashMap.class);

		Iterator<Entry<String, R>> it = rs.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, R> entry = it.next();
			String fullIdKey = entry.getKey();
			R r = entry.getValue();
			if (r.s == 2 || r.s == 4) {
				Object strToObj = valueSerializer.deserialize(r.o.toString().getBytes());
				r.o = strToObj;
			}

			Object id = keyIdMap.get(fullIdKey);
			if (id == null) {
				logger.error("Redis response result has error, RedisKey:" + fullIdKey
						+ "can not find relation entity ID, may be serializer error");
				continue;
			}
			idRsMap.put(id, r);
		}
		
		result = null;
		rs.clear();
		rs = null;
		
		keyIdMap.clear();
		keyIdMap = null;

		// 8.如果返回结果的记录数小于应返回的记录数，说明Redis运行发生了错误，可能是Key转移到另外的RedisServer节点
		if (idRsMap.size() < length) {
			logger.error("Request KeyValues length: " + length + "; Response result size: " + idRsMap.size());
		}

		return idRsMap;
	}

	@Override
	public String putOther(Object key, int second, Object value) {
		return redisClient.setex(metadata.getFullOtherKeyBytes(key), second, hashValueSerializer.serialize(value));
	}

	@Override
	public ValueWrapper getOther(Object key) {
		byte[] bytes = redisClient.get(metadata.getFullOtherKeyBytes(key));
		Object value = hashValueSerializer.deserialize(bytes);
		return toValueWrapper(value);
	}

	@Override
	public void delOther(Object key) {
		redisClient.del(metadata.getFullOtherKeyBytes(key));
	}

}
