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

package com.igeeksky.xcache.serializer.jackson;

import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.igeeksky.xcache.core.exception.SerializationFailedException;
import com.igeeksky.xcache.core.extend.ValueSerializer;
import com.igeeksky.xcache.core.util.BytesUtils;
import com.igeeksky.xcache.core.util.StringUtils;

import java.nio.charset.Charset;
import java.util.Objects;

/**
 * @author Tony.Lau
 * @blog: https://my.oschina.net/xcafe
 * @createTime 2017-03-06 20:38:33
 */
public class GenericJackson2JsonSerializer implements ValueSerializer<Object> {

    private final ObjectMapper mapper;

    private final Charset charset;

    public GenericJackson2JsonSerializer() {
        this(new ObjectMapper(), Charset.forName("UTF8"), (String) null);
    }

    public GenericJackson2JsonSerializer(Charset charset) {
        this(new ObjectMapper(), charset, (String) null);
    }

    public GenericJackson2JsonSerializer(ObjectMapper mapper) {
        this(mapper, Charset.forName("UTF8"), (String) null);
    }

    public GenericJackson2JsonSerializer(ObjectMapper mapper, Charset charset, String classPropertyTypeName) {
        Objects.requireNonNull(mapper, "ObjectMapper must not be null");
        Objects.requireNonNull(charset, "Charset must not be null");

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
        if (source == null) {
            return BytesUtils.EMPTY_BYTES;
        }
        if (source instanceof String || source instanceof Number) {
            return source.toString().getBytes(charset);
        }
        try {
            return mapper.writeValueAsBytes(source);
        } catch (JsonProcessingException e) {
            throw new SerializationFailedException("Could not write JSON: " + e.getMessage(), e);
        }
    }

    @Override
    public Object deserialize(byte[] source) {
        return deserialize(source, Object.class);
    }

    @Override
    public <T> T deserialize(byte[] source, Class<T> type) {
        Objects.requireNonNull(mapper, "Deserialization type must not be null! Please provide Object.class to default typing.");

        if (BytesUtils.isEmpty(source)) {
            return null;
        }
        try {
            return mapper.readValue(source, type);
        } catch (Exception e) {
            throw new SerializationFailedException("Could not write JSON: " + e.getMessage(), e);
        }
    }

}
