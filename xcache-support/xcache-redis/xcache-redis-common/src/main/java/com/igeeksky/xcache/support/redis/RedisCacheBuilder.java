package com.igeeksky.xcache.support.redis;

import com.igeeksky.xcache.core.*;
import com.igeeksky.xcache.core.extend.Serializer;
import com.igeeksky.xcache.core.extend.SerializerFunction;
import com.igeeksky.xcache.core.extend.StringSerializer;
import com.igeeksky.xcache.core.statistic.CacheStatisticsHolder;
import com.igeeksky.xcache.serializer.jackson.Jackson2JsonSerializer;


import java.util.function.Function;

/**
 * @author: Patrick.Lau
 * @date: 2021-06-21
 */
public class RedisCacheBuilder implements CacheBuilder {

    private final RedisWriter redisWriter;

    private SerializerFunction serializerFunction = Jackson2JsonSerializer::new;

    public RedisCacheBuilder(RedisWriter redisWriter) {
        this.redisWriter = redisWriter;
    }

    @Override
    public Cache<Object, Object> build(String name) {
        return null;
    }

    public <K> Serializer<K> keySerializer(Class<K> keyClazz) {
        if (keyClazz.equals(String.class)) {
            return (Serializer<K>) StringSerializer.UTF_8;
        }
        return serializerFunction.apply(keyClazz);
    }

    public <V> Serializer<V> valueSerializer(Class<V> valueClazz) {
        if (valueClazz.equals(String.class)) {
            return (Serializer<V>) StringSerializer.UTF_8;
        }
        return serializerFunction.apply(valueClazz);
    }

    @Override
    public <K, V> Cache<K, V> build(String name, Class<K> keyClazz, Class<V> valueClazz) {
        RedisCacheStore<K, V> cacheStore = new RedisCacheStore<>(redisWriter, keySerializer(keyClazz), valueSerializer(valueClazz));
        CacheConfig<K, V> cacheConfig = new CacheConfig<K, V>() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public CacheStore getCacheStore() {
                return cacheStore;
            }

            @Override
            public Function getLoader() {
                return null;
            }

            @Override
            public CacheStatisticsHolder getStatisticsHolder() {
                return null;
            }
        };
        return new DefaultCache<>(cacheConfig);
    }
}
