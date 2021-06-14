package com.igeeksky.xcache.support.redis;

import com.igeeksky.xcache.core.CacheStore;
import com.igeeksky.xcache.core.CacheValue;
import com.igeeksky.xcache.core.KeyValue;
import com.igeeksky.xcache.core.SimpleCacheValue;
import com.igeeksky.xcache.core.extend.KeyGenerator;
import com.igeeksky.xcache.core.extend.Serializer;
import com.igeeksky.xcache.core.util.CollectionUtils;
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

    private KeyGenerator<K, K> keyGenerator;
    private Serializer<V> valueSerializer;

    @Override
    public Mono<CacheValue<V>> get(K key) {
        // TODO 判断是否要压缩
        // TODO 判断value 是否为指定字符串空值
        // TODO 判断是否要加前缀
        byte[] stringKey = deConvertKey(key);
        return redisClient.get(stringKey).map(this::convertValue).map(this::toCacheValue);
    }

    @Override
    public Flux<KeyValue<K, CacheValue<V>>> getAll(Set<? extends K> keys) {
        if (CollectionUtils.isNotEmpty(keys)) {
            return redisClient.mget(toStringKeys(keys))
                    .map(kv -> new KeyValue<>(convertKey(kv.getKey()), toCacheValue(convertValue(kv.getValue()))));
        }
        return Flux.empty();
    }

    @Override
    public Mono<CacheValue<V>> put(K key, Mono<V> valueMono) {
        return valueMono.flatMap(value -> {
            byte[] strKey = deConvertKey(key);
            byte[] strValue = deConvertValue(value);
            if (null != strKey && null != strValue) {
                return redisClient.set(strKey, strValue).then(Mono.just(toCacheValue(value)));
            }
            return Mono.empty();
        });
    }

    @Override
    public Mono<Void> putAll(Mono<Map<? extends K, ? extends V>> keyValuesMono) {
        return keyValuesMono.flatMap(keyValues -> {
            Map<byte[], byte[]> keyValuesMap = new HashMap<>(keyValues.size());
            keyValues.forEach((key, value) -> {
                byte[] strKey = deConvertKey(key);
                byte[] strValue = deConvertValue(value);
                if (null != strKey && null != strValue) {
                    keyValuesMap.put(strKey, strValue);
                }
            });
            if (CollectionUtils.isNotEmpty(keyValuesMap)) {
                return redisClient.mset(keyValuesMap);
            }
            return Mono.empty();
        });
    }

    @Override
    public Mono<Void> remove(K key) {
        byte[] strKey = deConvertKey(key);
        if (null != strKey) {
            return redisClient.del(strKey).then();
        }
        return Mono.empty();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("redis string cache don't support clear operation");
    }

    private V convertValue(byte[] redisValue) {
        return valueSerializer.deserialize(redisValue);
    }

    private byte[] deConvertValue(V value) {
        return valueSerializer.serialize(value);
    }

    private byte[] deConvertKey(K key) {
        return keyGenerator.serialize(key);
    }

    private K convertKey(byte[] key) {
        return keyGenerator.deSerialize(key);
    }

    private CacheValue<V> toCacheValue(V value) {
        return new SimpleCacheValue<>(value);
    }

    private byte[][] toStringKeys(Set<? extends K> keys) {
        byte[][] keyArray = new byte[keys.size()][];
        int i = 0;
        for (K key : keys) {
            keyArray[i] = deConvertKey(key);
            i++;
        }
        return keyArray;
    }
}
