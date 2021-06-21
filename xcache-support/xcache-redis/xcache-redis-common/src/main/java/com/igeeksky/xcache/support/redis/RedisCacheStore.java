package com.igeeksky.xcache.support.redis;

import com.igeeksky.xcache.core.AbstractCacheStore;
import com.igeeksky.xcache.core.CacheValue;
import com.igeeksky.xcache.core.KeyValue;
import com.igeeksky.xcache.core.extend.Serializer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Map;

/**
 * @author Patrick.Lau
 * @date 2021-06-03
 */
public class RedisCacheStore<K, V> extends AbstractCacheStore<K, V> {

    private RedisWriter redisWriter;

    public RedisCacheStore(RedisWriter redisWriter, Serializer<K> keySerializer, Serializer<V> valueSerializer) {
        super(keySerializer, valueSerializer);
        this.redisWriter = redisWriter;
    }

    @Override
    protected Mono<CacheValue<V>> doGet(byte[] keyBytes) {
        return redisWriter.get(keyBytes).map(this::convertValue).map(this::toCacheValue);
    }

    @Override
    protected Flux<KeyValue<K, CacheValue<V>>> doGetAll(Mono<Collection<byte[]>> keys) {
        return keys.map(collection -> collection.toArray(new byte[collection.size()][]))
                .flatMapMany(ks -> redisWriter.mget(ks))
                .map(kv -> new KeyValue<>(convertKey(kv.getKey()), toCacheValue(convertValue(kv.getValue()))))
                .filter(kv -> kv.hasValue());
    }

    @Override
    protected Mono<Void> doPut(byte[] key, byte[] value) {
        return redisWriter.set(key, value);
    }

    @Override
    protected Mono<Void> doPutAll(Map<byte[], byte[]> keyValues) {
        return redisWriter.mset(keyValues);
    }

    @Override
    protected Mono<Void> doRemove(byte[] key) {
        return redisWriter.del(key).then();
    }

}
