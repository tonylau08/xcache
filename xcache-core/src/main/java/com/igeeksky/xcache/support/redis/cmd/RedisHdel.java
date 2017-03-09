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
 * @createTime 2017-03-08 00:26:25
 */
public class RedisHdel implements RedisCmd<Long> {
	
	private byte[] key;
	
	private byte[][] fields;
	
	public RedisHdel(byte[] key, byte[]... fields) {
		setParams(key, fields);
	}
	
	public void setParams(byte[] key, byte[]...fields){
		this.key = key;
		this.fields = fields;
	}
	
	public Long excute(Jedis jedis){
		return jedis.hdel(key, fields);
	}

	@Override
	public byte[] getKey() {
		return key;
	}
	
	public void release() {
		this.key = null;
		this.fields = null;
		RedisCmdHandler.INSTANCE.release(this);
	}

}
