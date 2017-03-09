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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.support.SimpleValueWrapper;

import com.R;
import com.igeeksky.xcache.core.RemoteCache;
import com.igeeksky.xcache.support.KeyValue;
import com.igeeksky.xcache.support.redis.RedisClusterClient;
import com.igeeksky.xcache.support.serializer.FSTSerializer;
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
public class RedisClusterCache implements RemoteCache {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final ValueSerializer<Object, Object> valueSerializer = new GenericJackson2JsonSerializer();

	private final KeySerializer<Object, Object> keySerializer = new StringKeySerializer();

	private final ValueSerializer<Object, Object> hashValueSerializer = new FSTSerializer();

	private final ScriptManager scriptManager;

	private final RedisCacheMetadata metadata;

	private String name;
	private long expiration = 86400;

	private RedisClusterClient redisClient;

	public RedisClusterCache(String name, long expiration, RedisClusterClient redisClient) {
		this.name = name;
		if (expiration > 0) {
			this.expiration = expiration;
		}
		this.redisClient = redisClient;
		this.scriptManager = new RedisClusterScriptManager(name, this.expiration);
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
		Object object = redisClient.evalOneKey(scriptManager.getGetCmpVerScript(), fullIdKeyBytes, verBytes);
		if (null != object && object instanceof byte[]) {
			R r = valueSerializer.deserialize((byte[]) object, R.class);
			if (r.s == 3) {
				redisClient.hdel(metadata.getKeySetBytes(), keySerializer.serialize(key));
			}
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
		redisClient.hset(metadata.getKeySetBytes(), keySerializer.serialize(key), BytesUtils.ONE_BYTES);
	}

	/**
	 * V1，V2，V3顺序写入数据库后，因为网络延迟等因素，以V3，V2，V1的倒序写入Redis，
	 * 如果版本不一致则删除，那么最后的Redis缓存的结果是V1,所以版本不一致并不删除Key。
	 */
	@Override
	public int putWithVersion(Object key, Object value, Long version) {
		byte[] fullKeyBytes = metadata.getFullIdKeyBytes(key);
		byte[] valueBytes = valueSerializer.serialize(value);
		byte[] verBytes = valueSerializer.serialize(version);
		Object state = redisClient.evalOneKey(scriptManager.getPutCmpVerScript(), fullKeyBytes, valueBytes, verBytes);
		Integer status;
		if (null == state || null == (status = NumUtils.getInteger(state))) {
			return 0;
		}

		if (status == 1) {
			redisClient.hset(metadata.getKeySetBytes(), metadata.getSuffixIdBytes(fullKeyBytes), BytesUtils.ONE_BYTES);
		} else if (status == 4) {
			redisClient.hdel(metadata.getKeySetBytes(), metadata.getSuffixIdBytes(fullKeyBytes));
		} else if (status == 5) {
			redisClient.hdel(metadata.getKeySetBytes(), metadata.getSuffixIdBytes(fullKeyBytes));
			throw new NullPointerException(new String(fullKeyBytes) + "remote old version is null");
		}

		if (null == version) {
			throw new NullPointerException(new String(fullKeyBytes) + "new version is null");
		}
		return status;
	}

	@Override
	public ValueWrapper putIfAbsent(Object key, Object value) {
		byte[] fullKeyBytes = metadata.getFullIdKeyBytes(key);
		byte[] valueBytes = valueSerializer.serialize(value);
		if (valueBytes == BytesUtils.EMPTY_BYTES) {
			return toValueWrapper(value);
		}

		Object state = redisClient.evalOneKey(scriptManager.getPutIfAbsentScript(), fullKeyBytes, valueBytes);
		Integer status;
		if (null == state || null == (status = NumUtils.getInteger(state))) {
			return toValueWrapper(value);
		}

		if (status == 1) {
			redisClient.hset(metadata.getKeySetBytes(), metadata.getSuffixIdBytes(fullKeyBytes), BytesUtils.ONE_BYTES);
			return null;
		} else {
			return toValueWrapper(value);
		}
	}

	@Override
	public void evict(Object key) {
		byte[] fullKeyBytes = metadata.getFullIdKeyBytes(key);
		redisClient.del(fullKeyBytes);
		redisClient.hdel(metadata.getKeySetBytes(), metadata.getSuffixIdBytes(fullKeyBytes));
	}

	@Override
	public void clear() {
		// 获取key集合
		Set<byte[]> set = redisClient.hkeys(metadata.getKeySetBytes());
		int length = 3;
		int size;
		byte[][] keys = null;
		if (set != null && (size = set.size()) > 0) {
			length = size + 3;
			keys = new byte[length][];
			int i = 0;
			byte[] prefix = metadata.getIdKeyPrifixBytes();
			for (byte[] idBytes : set) {
				keys[i] = BytesUtils.merge(prefix, idBytes);
				i++;
			}
		} else {
			keys = new byte[length][];
		}
		keys[length - 3] = metadata.getKeySetBytes();
		keys[length - 2] = metadata.getListKeyBytes();
		keys[length - 1] = metadata.getRelateKeyBytes();

		Long delNum = redisClient.del(keys);
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
	public Map<Object, R> getListCmpVersion(KeyValue[] kvs) {
		int length;
		if (null == kvs || (length = kvs.length) == 0) {
			return null;
		}

		// 1.记录RedisKey 与 实体ID的映射
		Map<String, Object> keyIdMap = new HashMap<String, Object>();

		// 2.拼接FullKey String 和 FullKeyBytes
		for (int i = 0; i < length; i++) {
			KeyValue kv = kvs[i];
			Object id = kv.getId();
			
			byte[] fullIdKeyBytes = metadata.getFullIdKeyBytes(id);
			String fullIdKey = new String(fullIdKeyBytes);
			
			keyIdMap.put(fullIdKey, id);
			kv.setKey(fullIdKey);
			kv.setFullIdKeyBytes(fullIdKeyBytes);
		}

		// 3.获取返回结果
		int size;
		List<Object> results = redisClient.evalListKey(scriptManager.getGetListCmpVerScript(), kvs);
		
		kvs = null;
		if (null == results || (size = results.size()) == 0) {
			return null;
		}

		// 4.实体ID 与 获取缓存的结果的映射
		Map<Object, R> idRsMap = new HashMap<Object, R>();
		// 5.远程版本小于本地版本的key集合
		List<byte[]> delKeys = new ArrayList<byte[]>();

		// 6.写入返回结果
		for (int i = 0; i < size; i++) {
			Object obj = results.get(i);
			@SuppressWarnings("unchecked")
			HashMap<String, R> rs = valueSerializer.deserialize((byte[]) obj, HashMap.class);

			Iterator<Entry<String, R>> it = rs.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, R> entry = it.next();
				Object fullIdKey = entry.getKey();
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
				
				if (r.s == 3) {
					delKeys.add(keySerializer.serialize(id));
				}
				
				idRsMap.put(id, r);
			}
			rs.clear();
			rs = null;
			obj = null;
		}
		results.clear();
		results = null;
		keyIdMap.clear();
		keyIdMap = null;

		// 7.删除KeySet中远程版本小于本地版本的ID
		int delSize = delKeys.size();
		if (delSize == 1) {
			redisClient.hdel(metadata.getKeySetBytes(), delKeys.get(0));
		} else if (delSize > 1) {
			redisClient.hdel(metadata.getKeySetBytes(), delKeys.toArray(new byte[delKeys.size()][]));
		}

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
