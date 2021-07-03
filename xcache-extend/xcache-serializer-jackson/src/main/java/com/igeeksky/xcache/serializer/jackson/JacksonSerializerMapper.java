package com.igeeksky.xcache.serializer.jackson;

import com.igeeksky.xcache.core.extend.Serializer;
import com.igeeksky.xcache.core.extend.SerializerMapper;

/**
 * @author Patrick.Lau
 * @date 2021-06-22
 */
public class JacksonSerializerMapper implements SerializerMapper {

    @Override
    public <T> Serializer<T> apply(Class<T> clazz) {
        return new Jackson2JsonSerializer<>(clazz);
    }
}
