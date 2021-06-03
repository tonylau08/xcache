package com.igeeksky.xcache.extend.redis;

import com.igeeksky.xcache.core.Cache;
import com.igeeksky.xcache.core.SimpleValueWrapper;
import com.igeeksky.xcache.core.support.Serializer;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionStage;

/**
 * @author Patrick.Lau
 * @date 2021-06-03
 */
public class CacheRedis<K, V> implements Cache<K, V> {

    private Serializer<V> serializer;
    private RedisOperators redisOperators;


    @Override
    public String getName() {
        return null;
    }

    @Override
    public ValueWrapper<V> get(K key) {
        String v = redisOperators.get((String) key);
        if (null != v) {
            V value = serializer.deserialize(v.getBytes(StandardCharsets.UTF_8));
            return new SimpleValueWrapper(value);
        }
        return null;
    }

    @Override
    public CompletionStage<ValueWrapper<V>> asyncGet(K key) {
        CompletionStage<String> completionStage = redisOperators.asyncGet((String) key);
        return completionStage.thenApply(v -> {
            if (null != v) {
                V value = serializer.deserialize(v.getBytes(StandardCharsets.UTF_8));
                return new SimpleValueWrapper(value);
            }
            return null;
        });
    }

    @Override
    public Map<K, V> getAll(Set<? extends K> keys) {
        return null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {

    }

    @Override
    public void put(K key, V value) {

    }

    @Override
    public ValueWrapper<V> putIfAbsent(K key, V value) {
        return null;
    }

    @Override
    public void remove(K key) {

    }

    @Override
    public void clear() {

    }
}
