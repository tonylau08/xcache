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

import java.nio.charset.Charset;

import org.springframework.core.serializer.support.SerializationFailedException;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.igeeksky.xcache.exception.NullOrEmptyKeyException;
import com.igeeksky.xcache.util.StringUtils;

/**
 * @author Tony.Lau
 * @blog: https://my.oschina.net/xcafe
 * @createTime 2017-03-06 08:41:27
 */
public class StringKeySerializer implements KeySerializer<Object, Object> {

	private final Charset charset;
	
	private final ObjectMapper mapper;
	
	public StringKeySerializer() {
		this(new ObjectMapper(), Charset.forName("UTF8"), (String)null);
	}

	public StringKeySerializer(Charset charset) {
		this(new ObjectMapper(), charset, (String)null);
	}

	public StringKeySerializer(ObjectMapper mapper) {
		this(mapper, Charset.forName("UTF8"), (String)null);
	}

	public StringKeySerializer(ObjectMapper mapper, Charset charset, String classPropertyTypeName) {
		Assert.notNull(mapper, "ObjectMapper must not be null!");
		Assert.notNull(charset, "Charset must not be null!");
		this.mapper = mapper;
		this.charset = charset;
		if (StringUtils.isNotEmpty(classPropertyTypeName)) {
			mapper.enableDefaultTypingAsProperty(DefaultTyping.NON_FINAL, classPropertyTypeName);
		} else {
			mapper.enableDefaultTyping(DefaultTyping.NON_FINAL, As.PROPERTY);
		}
	}

	@Override
	public byte[] serialize(Object source) {
		if(null == source){
			throw new NullOrEmptyKeyException();
		}
		if(source instanceof String || source instanceof Number){
			String str = source.toString();
			if(str.trim().isEmpty()){
				throw new NullOrEmptyKeyException();
			}
			return str.getBytes(charset);
		}
		try {
			return mapper.writeValueAsBytes(source);
		} catch (JsonProcessingException e) {
			throw new SerializationFailedException("Could not write JSON: " + e.getMessage(), e);
		}
	}

	@Override
	public String deserialize(byte[] bytes) {
		if(null == bytes || bytes.length == 0) return null;
		return new String(bytes, charset);
	}

}
