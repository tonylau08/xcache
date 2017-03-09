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

package com.igeeksky.xcache.support.redis.cmd;

import redis.clients.jedis.Jedis;

/**
 * @author Tony.Lau
 * @blog: https://my.oschina.net/xcafe
 * @createTime 2017-03-07 23:49:39
 */

public class RedisHset implements RedisCmd<Long> {
	
	private byte[] key;
	
	private byte[] field;
	
	private byte[] value;

	public RedisHset(byte[] key, byte[] field, byte[] value) {
		setParams(key, field, value);
	}
	
	public void setParams(byte[] key, byte[] field, byte[] value) {
		this.key = key;
		this.field = field;
		this.value = value;
	}

	@Override
	public Long excute(Jedis jedis) {
		return jedis.hset(key, field, value);
	}

	public void release() {
		this.key = null;
		this.field = null;
		this.value = null;
	}

	@Override
	public byte[] getKey() {
		return key;
	}

}
