package com.igeeksky.xcache.support.redis.lettuce;

import com.igeeksky.xcache.core.KeyValue;
import com.igeeksky.xcache.support.redis.RedisClientProxy;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import io.lettuce.core.codec.ByteArrayCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Objects;

/**
 * @author Patrick.Lau
 * @date 2021-06-12
 */
public class LettuceRedisClient implements RedisClientProxy {

    private final Logger log = LoggerFactory.getLogger(LettuceRedisClient.class);

    private StatefulRedisConnection<byte[], byte[]> connection;
    private RedisReactiveCommands<byte[], byte[]> reactive;

    public LettuceRedisClient() {
        RedisClient redisClient = RedisClient.create();
        this.connection = redisClient.connect(new ByteArrayCodec());
        this.reactive = connection.reactive();
    }

    @Override
    public Mono<byte[]> get(byte[] key) {
        return connection.reactive().get(key);
    }

    @Override
    public Flux<KeyValue<byte[], byte[]>> mget(byte[]... keys) {
        return connection.reactive()
                .mget(keys)
                .map(kv -> new KeyValue<>(kv.getKey(), kv.getValue()));
    }

    @Override
    public Mono<Void> set(byte[] key, byte[] value) {
        return connection.reactive()
                .set(key, value)
                .flatMap(result -> {
                    isSetSuccess(key, value, result);
                    return Mono.empty();
                });
    }

    @Override
    public Mono<Void> mset(Map<byte[], byte[]> keyValues) {
        return connection.reactive()
                .mset(keyValues)
                .then();
    }

    @Override
    public Mono<Long> del(byte[]... keys) {
        return connection.reactive().del(keys);
    }

    @Override
    public Mono<byte[]> hget(byte[] key, byte[] field) {
        return connection.reactive().hget(key, field);
    }

    @Override
    public Flux<KeyValue<byte[], byte[]>> hmget(byte[] key, byte[]... fields) {
        return connection.reactive()
                .hmget(key, fields)
                .map(kv -> new KeyValue<>(kv.getKey(), kv.getValue()));
    }

    @Override
    public Mono<Boolean> hset(byte[] key, byte[] field, byte[] value) {
        return connection.reactive().hset(key, field, value);
    }

    @Override
    public Mono<Void> hmset(byte[] key, Map<byte[], byte[]> map) {
        // TODO 可能失败？
        return connection.reactive().hmset(key, map).then();
    }

    @Override
    public Mono<Long> hdel(byte[] key, byte[]... fields) {
        return connection.reactive().hdel(key, fields);
    }

    private void isSetSuccess(byte[] key, byte[] value, String result) {
        if (!Objects.equals(OK, result)) {
            RuntimeException e = new RuntimeException("redis set error. key=" + key + ", value=" + value);
            log.error(e.getMessage(), e);
            throw e;
        }
    }

}
