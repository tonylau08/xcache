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
import java.util.List;

import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;

/**
 * @author Tony.Lau
 * @blog: https://my.oschina.net/xcafe
 * @createTime 2017-02-22 16:17:03
 */
public class XremoteCache implements RemoteCache {

	@SuppressWarnings("rawtypes")
	private RedisOperations redisOperations;
	// private final RedisCacheMetadata cacheMetadata;
	// private final CacheValueAccessor cacheValueAccessor;
	
	/**
	KEYS[1]: C_SYS_USER:id
	KEYS[2]: C_SYS_USER_VER
	ARGV[1]: value
	ARGV[2]: key
	*/
	String put = "local version = redis.call('HINCRBY',KEYS[2], ARGV[2], 1);"
			+ "if version > 0 then "
			+ "redis.call('SETEX',KEYS[1], ##expiration##, ARGV[1]);"
			+ "return version;else "
			+ "redis.call('HDEL', KEYS[2], id);"
			+ "redis.call('DEL', KEYS[1]);"
			+ "return 0;end;";
	
	private DefaultRedisScript<Long> putScript = new DefaultRedisScript<Long>(put, Long.class);

	private String name;
	private long expiration;
	private byte[] prefix;
	private String versionKey;
	
	public XremoteCache(String name, byte[] prefix, String versionKey, RedisOperations<? extends Object, ? extends Object> redisOperations,
			long expiration){
		this.name = name;
		this.expiration = expiration;
		this.put = put.replace("##expiration##", String.valueOf(expiration));
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Object getNativeCache() {
		return redisOperations;
	}

	@Override
	public ValueWrapper get(Object key) {
		// TODO Auto-generated method stub
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
		putAsVersion(key, value);
	}

	@Override
	public ValueWrapper putIfAbsent(Object key, Object value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void evict(Object key) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

	@Override
	public Long putAsVersion(Object key, Object value) {
		List<String> keys = new ArrayList<String>(2);
		String id = key.toString();
		keys.add(name + ":" + id);
		keys.add(name + "_VER");
		Object version = redisOperations.execute(putScript, keys, value, id);
		if(null != version){
			return (Long) version;
		}
		return null;
	}

}
