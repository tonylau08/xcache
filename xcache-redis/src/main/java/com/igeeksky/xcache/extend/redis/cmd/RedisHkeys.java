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

package com.igeeksky.xcache.extend.redis.cmd;

import redis.clients.jedis.Jedis;

import java.util.Set;

/**
 * @author Tony.Lau
 * @blog: https://my.oschina.net/xcafe
 * @createTime 2017-03-08 01:02:10
 */
public class RedisHkeys implements RedisCmd<Set<byte[]>> {

	private byte[] key;
	
	public RedisHkeys(byte[] key) {
		setParams(key);
	}
	
	public void setParams(byte[] key){
		this.key = key;
	}
	
	public Set<byte[]> excute(Jedis jedis){
		return jedis.hkeys(key);
	}

	@Override
	public byte[] getKey() {
		return key;
	}
	
	public void release() {
		this.key = null;
		RedisCmdHandler.INSTANCE.release(this);
	}

}
