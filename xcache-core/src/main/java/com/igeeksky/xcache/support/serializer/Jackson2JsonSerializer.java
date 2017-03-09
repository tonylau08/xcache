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

package com.igeeksky.xcache.support.serializer;

import org.springframework.core.serializer.support.SerializationFailedException;
import org.springframework.util.Assert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.igeeksky.xcache.util.BytesUtils;

/**
 * @author Tony.Lau
 * @blog: https://my.oschina.net/xcafe
 * @createTime 2017-03-06 20:38:33
 */
public class Jackson2JsonSerializer<T> implements ValueSerializer<T, Object>{

	private final ObjectMapper mapper;

	private final JavaType javaType;

	public Jackson2JsonSerializer(Class<T> classType) {
		this(new ObjectMapper(),  classType);
	}

	public Jackson2JsonSerializer(ObjectMapper mapper, Class<T> classType) {
		Assert.notNull(mapper, "ObjectMapper must not be null!");
		Assert.notNull(classType, "classType must not be null!");
		this.mapper = mapper;
		this.javaType = getJavaType(classType);
	}

	@Override
	public byte[] serialize(Object obj) {
		if(obj == null){
			return BytesUtils.EMPTY_BYTES;
		}
		try {
			return mapper.writeValueAsBytes(obj);
		} catch (JsonProcessingException e) {
			throw new SerializationFailedException("Could not write JSON: " + e.getMessage(), e);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public T deserialize(byte[] source) {
		if(BytesUtils.isEmpty(source)){
			return null;
		}
		try {
			return (T) mapper.readValue(source, javaType);
		} catch (Exception e) {
			throw new SerializationFailedException("Could not write JSON: " + e.getMessage(), e);
		}
	}
	
	protected JavaType getJavaType(Class<?> clazz) {
		return TypeFactory.defaultInstance().constructType(clazz);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <O> O deserialize(byte[] source, Class<O> type) {
		if(BytesUtils.isEmpty(source)){
			return null;
		}
		try {
			return (O) mapper.readValue(source, getJavaType(type));
		} catch (Exception e) {
			throw new SerializationFailedException("Could not write JSON: " + e.getMessage(), e);
		}
	}

}
