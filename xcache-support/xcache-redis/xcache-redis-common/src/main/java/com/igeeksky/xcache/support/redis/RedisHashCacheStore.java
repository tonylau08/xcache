package com.igeeksky.xcache.support.redis;

import com.igeeksky.xcache.core.AbstractCacheStore;
import com.igeeksky.xcache.core.CacheValue;
import com.igeeksky.xcache.core.KeyValue;
import com.igeeksky.xcache.core.extend.KeyGenerator;
import com.igeeksky.xcache.core.extend.Serializer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author Patrick.Lau
 * @date 2021-06-03
 */
public class RedisHashCacheStore<K, V> extends AbstractCacheStore<K, V> {

    private Charset charset = StandardCharsets.UTF_8;
    private RedisWriter redisClient;

    private KeyGenerator<K, K> keyGenerator;
    private Serializer<V> valueSerializer;

    private String name;

    private byte[][] redisHashKeys = new byte[16384][];

    public RedisHashCacheStore(Serializer<K> keySerializer, Serializer<V> valueSerializer) {
        super(keySerializer, valueSerializer);
    }

    @Override
    protected Mono<CacheValue<V>> doGet(byte[] field) {
        byte[] hashKey = selectHashKey(field);
        return redisClient.hget(hashKey, field).map(this::convertValue).map(this::toCacheValue);
    }

    private byte[] selectHashKey(byte[] field) {
        int index = CRC16.crc16(field);
        return redisHashKeys[index];
    }

    @Override
    protected Flux<KeyValue<K, CacheValue<V>>> doGetAll(Mono<Collection<byte[]>> fields) {
        return fields.map(collection -> {
            Map<byte[], List<byte[]>> hashKeyMap = new HashMap<>();
            for (byte[] field : collection) {
                byte[] hashKey = selectHashKey(field);
                List<byte[]> list = hashKeyMap.computeIfAbsent(hashKey, k -> new ArrayList<>());
                list.add(field);
            }
            return hashKeyMap;
        }).flatMapMany(hashKeyMap -> {
            Flux<KeyValue<byte[], byte[]>> result = Flux.empty();
            hashKeyMap.forEach((hashKey, list) -> {
                byte[][] fieldArray = list.toArray(new byte[list.size()][]);
                result.mergeWith(redisClient.hmget(hashKey, fieldArray));
            });
            return result.map(kv -> new KeyValue<>(convertKey(kv.getKey()), toCacheValue(convertValue(kv.getValue())))).filter(KeyValue::hasValue);
        });
    }

    @Override
    protected Mono<Void> doPut(byte[] field, byte[] hashValue) {
        return redisClient.hset(selectHashKey(field), field, hashValue).then();
    }

    @Override
    protected Mono<Void> doPutAll(Map<byte[], byte[]> keyValues) {
        Map<byte[], Map<byte[], byte[]>> hashKeyMap = new HashMap<>(keyValues.size());
        for (Map.Entry<byte[], byte[]> entry : keyValues.entrySet()) {
            byte[] field = entry.getKey();
            byte[] hashKey = selectHashKey(field);
            byte[] value = entry.getValue();
            Map<byte[], byte[]> splitMap = hashKeyMap.computeIfAbsent(hashKey, k -> new HashMap<>());
            splitMap.put(field, value);
        }

        Flux<Void> result = Flux.empty();
        Set<Map.Entry<byte[], Map<byte[], byte[]>>> set = hashKeyMap.entrySet();
        for (Map.Entry<byte[], Map<byte[], byte[]>> entry : set) {
            result = result.mergeWith(redisClient.hmset(entry.getKey(), entry.getValue()));
        }

        return result.then();
    }

    @Override
    protected Mono<Void> doRemove(byte[] field) {
        return redisClient.hdel(selectHashKey(field), field).then();
    }

}
