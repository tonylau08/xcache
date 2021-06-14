package com.igeeksky.xcache.support.redis;

import com.igeeksky.xcache.core.CacheStore;
import com.igeeksky.xcache.core.CacheValue;
import com.igeeksky.xcache.core.KeyValue;
import com.igeeksky.xcache.core.SimpleCacheValue;
import com.igeeksky.xcache.core.extend.KeyGenerator;
import com.igeeksky.xcache.core.extend.Serializer;
import com.igeeksky.xcache.core.util.CollectionUtils;
import io.lettuce.core.codec.CRC16;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author Patrick.Lau
 * @date 2021-06-03
 */
public class RedisHashCacheStore<K, V> implements CacheStore<K, V> {

    private Charset charset = StandardCharsets.UTF_8;
    private RedisClientProxy redisClient;

    private KeyGenerator<K, K> keyGenerator;
    private Serializer<V> valueSerializer;

    private String name;

    private byte[][] redisHashKeys = new byte[16384][];

    @Override
    public Mono<CacheValue<V>> get(K key) {
        // TODO 判断是否要压缩
        // TODO 判断value 是否为指定字符串空值
        // TODO 根据 name生成多个 HashKey，根据 field 计算该存入哪个 HashKey
        byte[] field = deConvertKey(key);
        byte[] hashKey = selectHashKey(field);
        return redisClient.hget(hashKey, field).map(this::convertValue).map(this::toCacheValue);
    }

    private byte[] selectHashKey(byte[] field) {
        int index = CRC16.crc16(field);
        return redisHashKeys[index];
    }

    @Override
    public Flux<KeyValue<K, CacheValue<V>>> getAll(Set<? extends K> keys) {
        if (CollectionUtils.isEmpty(keys)) {
            return Flux.empty();
        }

        byte[][] fields = toBytesKeys(keys);
        Map<byte[], List<byte[]>> hashKeyMap = new HashMap<>();
        for (byte[] field : fields) {
            byte[] hashKey = selectHashKey(field);
            List<byte[]> list = hashKeyMap.computeIfAbsent(hashKey, k -> new ArrayList<>());
            list.add(field);
        }

        Flux<KeyValue<byte[], byte[]>> result = Flux.empty();
        hashKeyMap.forEach((hashKey, list) -> {
            byte[][] fieldArray = list.toArray(new byte[list.size()][]);
            result.concatWith(redisClient.hmget(hashKey, fieldArray));
        });

        return result.map(kv -> new KeyValue<>(convertKey(kv.getKey()), toCacheValue(convertValue(kv.getValue()))));
    }

    @Override
    public Mono<CacheValue<V>> put(K key, Mono<V> valueMono) {
        return valueMono.flatMap(value -> {
            byte[] field = deConvertKey(key);
            byte[] hashKey = selectHashKey(field);
            byte[] hashValue = deConvertValue(value);
            if (null != field && null != hashValue) {
                return redisClient.hset(hashKey, field, hashValue).then(Mono.just(toCacheValue(value)));
            }
            return Mono.empty();
        });
    }

    @Override
    public Mono<Void> putAll(Mono<Map<? extends K, ? extends V>> keyValuesMono) {
        return keyValuesMono.flatMap(keyValues -> {
            Map<byte[], Map<byte[], byte[]>> hashKeyMap = new HashMap<>(keyValues.size());
            keyValues.forEach((key, value) -> {
                byte[] field = deConvertKey(key);
                byte[] hashKey = selectHashKey(field);
                byte[] hashValue = deConvertValue(value);
                if (null != field && null != hashValue) {
                    Map<byte[], byte[]> subMap = hashKeyMap.computeIfAbsent(hashKey, k -> new HashMap<>(keyValues.size()));
                    subMap.put(field, hashValue);
                }
            });
            Flux<Void> result = Flux.empty();
            hashKeyMap.forEach((hashKey, map) -> {
                result.concatWith(redisClient.hmset(hashKey, map));
            });
            return result.collectList().then();
        });
    }

    @Override
    public Mono<Void> remove(K key) {
        byte[] field = deConvertKey(key);
        if (null != field) {
            byte[] hashKey = selectHashKey(field);
            return redisClient.hdel(hashKey, field).then();
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

    private byte[][] toBytesKeys(Set<? extends K> keys) {
        byte[][] fields = new byte[keys.size()][];
        int i = 0;
        for (K key : keys) {
            fields[i] = deConvertKey(key);
            i++;
        }
        return fields;
    }
}
