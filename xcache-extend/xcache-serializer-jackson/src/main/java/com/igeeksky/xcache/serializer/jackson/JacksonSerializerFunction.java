package com.igeeksky.xcache.serializer.jackson;

import com.igeeksky.xcache.core.extend.Serializer;
import com.igeeksky.xcache.core.extend.SerializerFunction;

/**
 * @author Patrick.Lau
 * @date 2021-06-22
 */
public class JacksonSerializerFunction implements SerializerFunction {

    @Override
    public <T> Serializer<T> apply(Class<T> clazz) {
        return new Jackson2JsonSerializer<>(clazz);
    }
}
