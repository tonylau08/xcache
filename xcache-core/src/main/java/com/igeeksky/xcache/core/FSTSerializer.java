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

import java.nio.charset.Charset;

import org.nustaq.serialization.FSTConfiguration;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

/**
 * @author Tony.Lau
 * @blog: https://my.oschina.net/xcafe
 * @createTime 2017-02-23 01:56:40
 */
public class FSTSerializer implements RedisSerializer<Object> {
	
	//private final RedisSerializer<Object> redisSerializer = new JdkSerializationRedisSerializer();
	
	private final FSTConfiguration configuration = FSTConfiguration.createDefaultConfiguration();

	private final Charset charset;

	public FSTSerializer() {
		this.charset = Charset.forName("UTF8");
	}
	
	@Override
	public byte[] serialize(Object t) throws SerializationException {
		if(null != t){
			if(t instanceof String || t instanceof Long || t instanceof Integer || t instanceof Byte){
				return t.toString().getBytes(charset);
			}else{
				return configuration.asByteArray(t);
			}
		}
		return null;
	}

	@Override
	public Object deserialize(byte[] bytes) throws SerializationException {
		if(null != bytes && bytes.length!=0){
			return configuration.asObject(bytes);
			//return redisSerializer.serialize(t);
		}
		return null;
	}

}
