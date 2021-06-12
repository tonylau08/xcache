package com.igeeksky.xcache.support.redis;

import com.igeeksky.xcache.core.CacheStore;
import com.igeeksky.xcache.core.CacheValue;
import com.igeeksky.xcache.core.KeyValue;
import com.igeeksky.xcache.core.SimpleCacheValue;
import com.igeeksky.xcache.core.extend.KeyGenerator;
import com.igeeksky.xcache.core.extend.Serializer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Patrick.Lau
 * @date 2021-06-03
 */
public class RedisCacheStore<K, V> implements CacheStore<K, V> {

    private Charset charset = StandardCharsets.UTF_8;
    private RedisClientProxy redisClient;

    private KeyGenerator<String, K> keyGenerator;
    private Serializer<V> valueSerializer;

    @Override
    public Mono<CacheValue<V>> get(K key) {
        // TODO 判断使用 Hash 还是 String 保存数据
        // TODO 判断是否要压缩
        // TODO 判断value 是否为指定字符串空值
        // TODO 判断是否要加前缀
        String stringKey = serializeKey(key);
        return redisClient.get(stringKey).map(this::deserializeValue).map(this::toCacheValue);
    }

    @Override
    public Flux<KeyValue<K, CacheValue<V>>> getAll(Set<? extends K> keys) {
        // TODO 判空
        return redisClient.mget(toStringKeys(keys))
                .map(kv -> new KeyValue<>(deserializeKey(kv.getKey()), toCacheValue(deserializeValue(kv.getValue()))));
    }

    @Override
    public Mono<CacheValue<V>> put(K key, Mono<V> value) {
        return null;
    }

    @Override
    public Mono<Void> putAll(Mono<Map<? extends K, ? extends V>> keyValues) {
        return null;
    }

    @Override
    public Mono<CacheValue<V>> remove(K key) {
        return null;
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("redis string cache don't support clear operation");
    }

    private V deserializeValue(String redisValue) {
        return valueSerializer.deserialize(redisValue.getBytes(charset));
    }

    private String serializeValue(V value) {
        return new String(valueSerializer.serialize(value), charset);
    }

    private String serializeKey(K key) {
        return keyGenerator.deConvert(key);
    }

    private K deserializeKey(String key) {
        return keyGenerator.convert(key);
    }

    private CacheValue<V> toCacheValue(V value) {
        return new SimpleCacheValue<>(value);
    }

    private Map<K, CacheValue<V>> toCacheValueMap(List<KeyValue<String, String>> keyValues) {
        Map<K, CacheValue<V>> cacheValueMap = new HashMap<>(keyValues.size());
        keyValues.forEach(kv -> {
            if (null != kv) {
                V value = deserializeValue(kv.getValue());
                cacheValueMap.put(deserializeKey(kv.getKey()), toCacheValue(value));
            }
        });
        return cacheValueMap;
    }

    private String[] toStringKeys(Set<? extends K> keys) {
        String[] stringKeys = new String[keys.size()];
        int i = 0;
        for (K key : keys) {
            stringKeys[i] = serializeKey(key);
            i++;
        }
        return stringKeys;
    }
}
