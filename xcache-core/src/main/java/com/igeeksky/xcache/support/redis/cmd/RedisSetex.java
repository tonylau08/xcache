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
 * @createTime 2017-03-08 00:39:58
 */

public class RedisSetex implements RedisCmd<String> {

	private byte[] key;
	
	private int seconds;
	
	private byte[] value;
	
	public RedisSetex(byte[] key , int seconds, byte[] value) {
		setParams(key, seconds, value);
	}
	
	public void setParams(byte[] key, int seconds, byte[] value) {
		this.key = key;
		this.seconds = seconds;
		this.value = value;
	}
	
	@Override
	public String excute(Jedis jedis) {
		return jedis.setex(key, seconds, value);
	}

	@Override
	public byte[] getKey() {
		return key;
	}
	
	public void release() {
		this.key = null;
		this.seconds = -1;
		this.value = null;
		RedisCmdHandler.INSTANCE.release(this);
	}

}
