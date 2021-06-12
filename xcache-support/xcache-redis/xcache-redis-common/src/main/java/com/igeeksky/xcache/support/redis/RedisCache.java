package com.igeeksky.xcache.support.redis;

import com.igeeksky.xcache.core.CacheStore;
import com.igeeksky.xcache.core.CacheValue;
import com.igeeksky.xcache.core.KeyValue;
import com.igeeksky.xcache.core.SimpleCacheValue;
import com.igeeksky.xcache.core.extend.KeyGenerator;
import com.igeeksky.xcache.core.extend.Serializer;
import com.igeeksky.xcache.core.util.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * @author Patrick.Lau
 * @date 2021-06-03
 */
public class RedisCache<K, V> implements CacheStore<K, V> {

    private Charset charset = StandardCharsets.UTF_8;
    private RedisClientProxy redisClient;

    private KeyGenerator<String, K> keyGenerator;
    private Serializer<V> valueSerializer;

    @Override
    public CacheValue<V> get(K key) {
        // TODO 判断使用 Hash 还是 String 保存数据
        // TODO 判断是否要压缩
        // TODO 判断value 是否为指定字符串空值
        // TODO 判断是否要加前缀
        String redisValue = redisClient.get(serializeKey(key));
        V value = deserializeValue(redisValue);
        return toCacheValue(value);
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

    @Override
    public Map<K, CacheValue<V>> getAll(Set<? extends K> keys) {
        if (CollectionUtils.isNotEmpty(keys)) {
            String[] stringKeys = toStringKeys(keys);
            List<KeyValue<String, String>> keyValues = redisClient.mget(stringKeys);
            return toCacheValueMap(keyValues);
        }
        return Collections.emptyMap();
    }

    @NotNull
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

    @NotNull
    private String[] toStringKeys(Set<? extends K> keys) {
        String[] stringKeys = new String[keys.size()];
        int i = 0;
        for (K key : keys) {
            stringKeys[i] = serializeKey(key);
            i++;
        }
        return stringKeys;
    }

    @Override
    public CacheValue<V> put(K key, V value) {
        String redisValue = serializeValue(value);
        redisClient.set(serializeKey(key), redisValue);
        return toCacheValue(value);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> keyValues) {
        if (CollectionUtils.isNotEmpty(keyValues)) {
            Map<String, String> redisMap = new HashMap<>();
            keyValues.forEach((key, value) -> {
                redisMap.put(serializeKey(key), serializeValue(value));
            });
            redisClient.mset(redisMap);
        }
    }

    @Override
    public void remove(K key) {
        redisClient.del(serializeKey(key));
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("redis string cache don't support clear operation");
    }

    @Override
    public CompletableFuture<CacheValue<V>> asyncGet(K key) {
        return redisClient
                .asyncGet(serializeKey(key))
                .thenApply(this::deserializeValue)
                .thenApply(this::toCacheValue);
    }

    @Override
    public CompletableFuture<Map<K, CacheValue<V>>> asyncGetAll(Set<? extends K> keys) {
        return redisClient
                .asyncMget(toStringKeys(keys))
                .thenApply(this::toCacheValueMap);
    }

    @Override
    public CompletableFuture<Void> asyncPutAll(CompletableFuture<Map<? extends K, ? extends V>> keyValuesFuture) {
        return null;
    }

    @Override
    public CompletableFuture<Void> asyncPut(K key, CompletableFuture<V> valueFuture) {
        return null;
    }

    @Override
    public CompletableFuture<Void> asyncRemove(K key) {
        return null;
    }

    @Override
    public Mono<CacheValue<V>> reactiveGet(K key) {
        return null;
    }

    @Override
    public Flux<KeyValue<K, CacheValue<V>>> reactiveGetAll(Set<? extends K> keys) {
        return null;
    }

    @Override
    public Mono<Void> reactivePutAll(Mono<Map<? extends K, ? extends V>> keyValues) {
        return null;
    }

    @Override
    public Mono<Void> reactivePut(K key, Mono<V> value) {
        return null;
    }

    @Override
    public Mono<Void> reactiveRemove(K key) {
        return null;
    }

    @Override
    public void close() throws IOException {

    }
}
