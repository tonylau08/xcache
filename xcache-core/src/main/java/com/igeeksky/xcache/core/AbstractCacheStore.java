package com.igeeksky.xcache.core;

import com.igeeksky.xcache.core.extend.Serializer;
import com.igeeksky.xcache.core.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

/**
 * @author Patrick.Lau
 * @date 2021-06-22
 */
public abstract class AbstractCacheStore<K, V> implements CacheStore<K, V> {

    private Serializer<K> keySerializer;
    private Serializer<V> valueSerializer;

    // TODO 判断是否要压缩
    private boolean compress;
    // TODO 判断是否要加前缀
    private boolean usePrefix;

    public AbstractCacheStore(Serializer<K> keySerializer, Serializer<V> valueSerializer) {
        this.keySerializer = keySerializer;
        this.valueSerializer = valueSerializer;
    }

    @Override
    public Mono<CacheValue<V>> get(K key) {
        byte[] keyBytes = deConvertKey(key);
        if (null != keyBytes && keyBytes.length > 0) {
            return doGet(keyBytes);
        }
        return Mono.error(new RuntimeException("Key bytes must not be null or empty."));
    }

    protected abstract Mono<CacheValue<V>> doGet(byte[] keyBytes);

    @Override
    public Flux<KeyValue<K, CacheValue<V>>> getAll(Set<? extends K> keys) {
        Mono<Collection<byte[]>> mono = Mono.justOrEmpty(keys)
                .map(this::deConvertKeys)
                .filter(CollectionUtils::isNotEmpty);
        return doGetAll(mono);
    }

    protected abstract Flux<KeyValue<K, CacheValue<V>>> doGetAll(Mono<Collection<byte[]>> keys);

    @Override
    public Mono<CacheValue<V>> put(K key, V value) {
        return Mono.just(new KeyValue<>(key, value))
                .map(kv -> new KeyValue<>(deConvertKey(kv.getKey()), deConvertValue(kv.getValue())))
                .filter(kv -> (null != kv.getKey() && kv.getKey().length > 0 && null != kv.getValue()))
                .flatMap(kv -> doPut(kv.getKey(), kv.getValue()).then(Mono.just(toCacheValue(value))));
    }

    protected abstract Mono<Void> doPut(byte[] key, byte[] value);

    @Override
    public Mono<Void> putAll(Mono<Map<? extends K, ? extends V>> keyValuesMono) {
        return keyValuesMono
                .map(keyValues -> {
                    Map<byte[], byte[]> keyValuesMap = new HashMap<>(keyValues.size());
                    keyValues.forEach((key, value) -> {
                        byte[] keyBytes = deConvertKey(key);
                        byte[] valueBytes = deConvertValue(value);
                        if (null != keyBytes && keyBytes.length > 0 && null != valueBytes) {
                            keyValuesMap.put(keyBytes, valueBytes);
                        }
                    });
                    return keyValuesMap;
                })
                .filter(CollectionUtils::isNotEmpty)
                .flatMap(this::doPutAll);
    }

    protected abstract Mono<Void> doPutAll(Map<byte[], byte[]> keyValues);

    @Override
    public Mono<Void> remove(K key) {
        return Mono.justOrEmpty(key)
                .filter(Objects::nonNull)
                .map(this::deConvertKey)
                .filter(Objects::nonNull)
                .flatMap(this::doRemove);
    }

    protected abstract Mono<Void> doRemove(byte[] key);

    protected CacheValue<V> toCacheValue(V value) {
        return new SimpleCacheValue<>(value);
    }

    protected V convertValue(byte[] redisValue) {
        // TODO 判断value 是否为指定字符串空值
        return valueSerializer.deserialize(redisValue);
    }

    protected byte[] deConvertValue(V value) {
        return valueSerializer.serialize(value);
    }

    protected byte[] deConvertKey(K key) {
        return keySerializer.serialize(key);
    }

    protected K convertKey(byte[] key) {
        return keySerializer.deserialize(key);
    }

    protected Collection<byte[]> deConvertKeys(Set<? extends K> keys) {
        List<byte[]> bytesList = new ArrayList<>(keys.size());
        for (K key : keys) {
            if (null == key) {
                continue;
            }
            byte[] bytes = deConvertKey(key);
            if (null == bytes || bytes.length == 0) {
                continue;
            }
            bytesList.add(bytes);
        }
        return bytesList;
    }

}
